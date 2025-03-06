#!/bin/bash

rm -r "$HOME/.m2/repository/com/github/ggruzdov" 2> /dev/null &&
docker rmi ggruzdov/outbox-order:1.0 &&
docker rmi ggruzdov/outbox-delivery:1.0