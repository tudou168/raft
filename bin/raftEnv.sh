#!/usr/bin/env bash


#   set classPath
for i in ${raft_bin_dir}/../lib/*.jar
do
    CLASSPATH="$i:$CLASSPATH"
done
