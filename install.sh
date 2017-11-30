#!/usr/bin/env bash

echo    "install the raft ..."

install_path="${BASH_SOURCE-$0}"
install_path="$(dirname "${install_path}")"

mvn clean
mvn install
echo "copy the dependencies jars into lib..."
mvn dependency:copy-dependencies -DoutputDirectory=${install_path}/lib


