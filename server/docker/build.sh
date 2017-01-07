#!/bin/bash

cp ../build/distributions/server-*.tar restwars.tar
cp ../config.yaml config.yaml

sudo docker build -t docker.io/phxql/restwars .
