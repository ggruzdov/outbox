#!/bin/bash

cd outbox-starter &&
./mvnw clean install -DskipTests=true &&

cd ../order-service &&
./mvnw clean package -DskipTests=true &&
docker build --no-cache -t ggruzdov/outbox-order:1.0 . &&

cd ../delivery-service &&
./mvnw clean package -DskipTests=true &&
docker build --no-cache -t ggruzdov/outbox-delivery:1.0 .