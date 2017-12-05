#!/usr/bin/env bash


raft_bin="${BASH_SOURCE-$0}"
raft_bin="$(dirname "${raft_bin}")"
raft_bin_dir="$(cd "${raft_bin}"; pwd)"



echo "set env..."
. $raft_bin_dir/raftEnv.sh

echo "start raft server..."
java -Draft.cfg.file=${raft_cfg_dir}/${raft_cfg} -cp $CLASSPATH/raft-core-1.0.jar com.tongbanjie.raft.core.bootstrap.RaftClientMainBootstrap "$@"