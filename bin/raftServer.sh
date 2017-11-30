#!/usr/bin/env bash


localServer=$1

servers=$2

logPath=$3

if  [ ! -n "$localServer" ]; then
    echo    "local server not allow null "

    exit 1
    
fi

echo    $localServer


if  [ ! -n "$servers" ]; then
    echo    "servers not allow null "

    exit 1

fi


if  [ ! -n "$logPath" ]; then
    echo    "logPath not allow null "

    exit 1

fi



raft_bin="${BASH_SOURCE-$0}"
raft_bin="$(dirname "${raft_bin}")"
raft_bin_dir="$(cd "${raft_bin}"; pwd)"



echo "set env..."
. $raft_bin_dir/raftEnv.sh

echo "start raft server..."
java -cp $CLASSPATH/raft-core-1.0.jar com.tongbanjie.raft.core.bootstrap.RaftServerMainBootstrap $localServer $servers $logPath/$localServer .raft