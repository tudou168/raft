#!/usr/bin/env bash

raft_cfg_dir="${raft_bin_dir}/../conf"

raft_cfg=raft.cfg

echo "Using $raft_cfg_dir/$raft_cfg..."

CLASSPATH="$CLASSPATH:$raft_cfg_dir"

#   set classPath
for i in ${raft_bin_dir}/../lib/*.jar
do
    CLASSPATH="$i:$CLASSPATH"
done
