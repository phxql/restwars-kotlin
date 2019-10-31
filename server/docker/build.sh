#!/bin/bash

cp ../build/distributions/server-*.tar restwars.tar
cp ../gameConfig.yaml gameConfig.yaml

sudo docker build -t docker.io/phxql/restwars .
