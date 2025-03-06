# Problem
In distributed systems one of the biggest challenges is consistency across multiple services. Let's imagine we have an e-commerce 
application where customers place orders and get them delivered. When a customer places and order we store the data 
into our database, and we have to notify other services that some data has been changed so they do appropriate logic for the 
event. That is a classic event-driven architecture. And here is a challenge: how do we achieve data consistency among services? 
One reliable solution is [outbox-pattern](https://microservices.io/patterns/data/transactional-outbox.html). 
However, there can be different implementations of it. In my career I have applied three viable variants and 
all of them have pros and cons. Let's have a look at them.

## Outbox-pattern implementations
### Change data capture mechanism(CDC) via [Debezium](https://debezium.io).

**Pros** 
- Makes our applications awareless about infrastructure complexity, e.g. messaging system such as Kafka so we basically
work with plain old relational databases only. Thereby we have much simpler cognitive complexity in app codebase.
- Probably the most real-time message transmission.

**Cons**
- Steep learning curve. You have to be quite a DBA and DevOps engineer to set up everything properly. Moreover, some configuration properties
can be tricky.
- Not that flexible configuration and quite complex.
- Additional infrastructure complexity(components, monitoring, maintenance).
- In High Availability(HA) systems likely prone to data loss while failover switching.

### Separate scheduler(like a _classic_ outbox relay)

**Pros**
- Relatively simple implementation
- Full control and flexibility of message delivery(different brokers, formats etc.).

**Cons**
- Additional component in infrastructure and likely lots of code duplication.
- Scalability can be tricky(but solvable).
- Delay between data storing and message delivery. Requires proper configuration such as how often to read data from database.
- Extra DB read in order to send messages to a message broker.

### Built-in solution

**Pros**
- Real-time message delivery. Application produces messages just after transaction gets commited. 
- Full control and flexibility of message delivery.

**Cons**
- Application complexity, additional code base for message delivery.
- Reliable implementation can be tricky to tackle all corner cases.
- Takes additional actions to make an application scalable.

## The choice
_Debezium_ may look like a perfect choice, and yes, it is a very good tool. In one project I was using it for a few years in production,
but this tool is the most complicated out of the three. In addition, with time I realized that such tool is a bit overkill 
just for event streaming. The biggest disadvantage with _Separate scheduler_ is that we have slight delay of message delivery, 
and it can be tricky to fine tune it. The third solution allows us to send messages without delay and gives full flexibility, 
although it comes with some caveats. In this demo project we implement mix of the second and third approach.

## Technical implementation
- Postgres as the primary database
- Spring Boot 3.4.3
- Java 21
- Flyway migrations
- Kafka(3 brokers cluster)
- Outbox pattern
- Own spring-boot-starter
- Graceful exception and message duplication handling from consumer side
- Docker and Docker Compose

# Architecture
![](outbox-integration.drawio.svg)

1. A client places an order via HTTP.
2. The request is handled by OrderService. It stores data into operational tables and outbox table transactionally.
3. When the transaction gets commited it sends a message to Kafka.
4. When a message is successfully sent it marks the outbox record in DB as "processed".
5. Then all of interested services listen to the event(in our case it is only DeliveryService).
6. DeliveryService does its business logic. Commits message offset if everything went fine, otherwise retries the message.

## Sub-problems to cover
1. When we are trying to send a message and message broker is not available or there is a network issue.
2. We may send a message to a broker, but we may fail to commit the message as processed in our DB.
3. If we have quite a high load we do not want to overwhelm DB and commit each message.
4. Unfortunately, message duplication is inevitable, so we have to handle them gracefully.
5. We have to handle exceptions in a consumer properly. Retry all _system_ exceptions and define precise retry mechanism 
for _business_ exceptions, and, most importantly, not to get into infinite retry loop.
6. We have to maintain the same trace context across microservices no matter what.
7. How to keep our application scalable(as we have a scheduler).

## Outbox-starter
![](outbox-starter.drawio.svg)

<span style="color:Aqua">**Parent app flow(order-service in our case)**</span>
1. Each time an application wants to change some data in its DB and notify other services about the change, it does 
necessary logic in one transaction and saves some payload as a message body via OutboxService bean. 
2. OutboxService retrieves current tracing data to be able to restore traceId in case of failures.
3. OutboxService saves data into Outbox table(configurable, default is _outbox_).
4. After transaction commit the parent app sends a message via OutboxSender. By default, there is a KafkaOutboxSender configured,
but can be overridden by any other implementation.

<span style="color:LightPink">**Outbox reconcile flow**</span>
1. There is a Spring Boot scheduler configured alongside with SchedLock library to make our application consistent in case 
of scaling. Scheduled interval is configurable, default is 3 minutes. Note that **it should not run too often**, every 2 minutes 
is the lowest interval. Why do we need the scheduler? Well, in distributed environment components are usually ephemeral, 
so there can be cases when we send a message but don't commit it in DB, so the scheduler will handle it. Since we don't know
what was the reason of failure(whether it was delivered or not) **message duplication is inevitable**! One way to handle it 
is to have some idempotency identifier. In our case we have a unique constraint for _order_id_ in DeliveryService, so we handle 
message duplication gracefully. In the worst case scenario we can track a message log in additional table.
2. The Scheduler reads unprocessed messages from DB in cursor-based manner not to get OutOfMemoryError if there are too many records.
3. The Scheduler restores trace context via TraceContextUtil.
4. The Scheduler sends a message via OutboxSender in original trace context.

<span style="color:LightGreen">**Message send flow**</span>
1. OutboxSender sends a message to Kafka with default producer retry policy.
2. OutboxSender commits the message. In this demo we use BufferedOutboxCommiter which puts messages into a queue and commit them by batches overtime 
or when buffer size is reached(size is configurable, default is 10). We have additional scheduler to autocommit processed messages in order not to 
keep them uncommited for too long if we do not have orders for some time.
3. OutboxCommiter sets _processed_at_ field in DB to all buffered outbox UUIDs to prevent repeatable sending.

## Repository overview
There are four directories:
1. __order-service__ - main order processor, handles requests from clients.
2. __delivery-service__ - arranges deliveries for orders. Asynchronously communicates with order-service via Kafka.
3. __outbox-starter__ - library which incorporates all the components we need for reliable outbox-pattern.
4. __infra__ - docker compose files for databases and Kafka cluster.

# Getting started

### Prerequisites
- Unix-like operating system(for Windows just manually execute commands from shell scripts and use `mvnw.cmd` instead)
- Docker and Docker Compose
- `jq` tool (optional, for pretty-printing JSON responses in the usage examples down below)

### Installation

1. **Build the project**
```bash
./build.sh
```
If you want to run the project in your IDE, then build locally _outbox-starter_. Switch into outbox-starter directory 
and run `mvn clean install`.

2. **Start the application**
```bash
docker compose up -d
```
Wait for a minute while everything is starting.  

Order API: `http://localhost:8080/orders`

Orders database credentials:
```
URL: jdbc:postgresql://localhost:5432/orders
Username: user
Password: password
```

Delivery API: `http://localhost:8081/deliveries`

Orders database credentials:
```
URL: jdbc:postgresql://localhost:5433/deliveries
Username: user
Password: password
```

Kafka UI: `http://localhost:9999`

3. **Stop the project**
```bash
docker compose down
```

4. **Clean up**
```bash
./clean.sh  # Removes local docker images and outbox-starter from maven repo
```

## Usage examples
**NOTE:** in delivery-service there is a random exception throw to imitate possible issues and check resiliency of the app.
In addition, it allows us to create many orders, say 20, and get message lag in Kafka. So we can test some chaos scenario, 
e.g. reboot brokers or consumer(delivery-service). And yes, delivery-service is scaled up to 3 instances.

### Create an order
```bash
curl --location --request POST 'http://localhost:8080/orders' \
--header 'Content-Type: application/json' \
--data-raw '{
    "requestId": "0375c0db-0dcd-420b-b639-a4be487193ca",
    "customerId": 106,
    "totalPrice": 10001
}' | jq .
```
Wait a few sec(remember, there is a random exception can occur, so delivery-service may retry message a few times) 
and then check that order delivery has been created:
```bash
curl --location --request GET 'http://localhost:8081/deliveries?orderId=1' | jq .
```

### Chaos engineering
First, let's break producer side
1. Stop the entire kafka cluster `docker compose stop kafka1 kafka2 kafka3`.
2. Create an order(or few) and see that they have been stored in orders database. 
All the orders don't have deliveries(as Kafka is down).
3. Restart order-service(we want maximum hardcore, don't we?) `docker compose restart order-svc`.
4. Start kafka cluster `docker compose start kafka1 kafka2 kafka3` and after a few sec see that each order has its delivery in deliveries DB.

Second, let's break consumer side:
1. We need to imitate message lag in Kafka. To do so just produce 20+ messages fast.
2. Restart delivery-service `docker compose restart delivery-svc1 delivery-svc2 delivery-svc3`.
3. After a while see that all deliveries have been created.

In addition, you can produce message duplicate via Kafka UI and see that it will be skipped and there is no infinite retry loop.