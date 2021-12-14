#!/bin/sh
set -x
mvn package
docker build -t tree-hole ./target/
export tag=$(date '+%Y%m%d-%H%M%S')
docker tag tree-hole:latest docker.io/ssdsyracuse/tree-hole:${tag}
docker push docker.io/ssdsyracuse/tree-hole:${tag}
