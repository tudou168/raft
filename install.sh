#!/usr/bin/env bash

echo    "install the raft ..."

echo $install_path

mvn clean
mvn install
echo "copy the dependencies jars into lib..."
mvn dependency:copy-dependencies -DoutputDirectory=../lib/


