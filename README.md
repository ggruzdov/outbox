# Problem
In distributed systems one of the biggest challenges is consistency across multiple services. Let's imagine we have an e-commerce 
application where customers put orders and get them delivered. When a customer places and order we store the data  
into our database, and we have to notify other services that some data has been changed so they do appropriate logic for the 
event. That is a classic event-driven architecture. And here is a challenge: how do we achieve data consistency between services? 
One reliable solution is [outbox-pattern](https://microservices.io/patterns/data/transactional-outbox.html). 
However, there can be different implementations of it. In my career we have applied three variants and 
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
- In High Availability(HA) systems prone to data loss while failover switching.

### Separate scheduler

**Pros**
- Relatively simple implementation
- Full control and flexibility of message delivery(different brokers, formats etc.).

**Cons**
- Additional component in infrastructure and kind of "reinvention the wheel".
- Scalability can be tricky but solvable.
- Delay between data storing and message delivery. Requires proper configuration such how often to read data from database.
- Additional DB read in order to send messages to message broker

### Built-in solution

**Pros**
- Real-time message delivery. Application produces message just after transaction gets commited. 
- Full control and flexibility of message delivery.

**Cons**
- Application complexity, additional code base for message delivery.
- Reliable implementation can be tricky to tackle all corner cases.
- Takes additional actions to make an application scalable.

## The choice
_Debezium_ may seem look like a perfect choice, and yes, it is. In one project I wa using it for a few years in production,
but this tool is the most complicated out of the three. In addition with time I realized that such tool is a bit overkill 
just for event streaming. The biggest disadvantage with _Separate scheduler_ is that we have slight delay, and it can be tricky to fine tune it. 
The third solution allows us to send messages without delay and gives full flexibility, although it comes with some caveats. 
This project is precisely devoted to showcase the solution.

