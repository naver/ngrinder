#!/usr/bin/env bash
echo "copying ngrinder-controller"
mkdir -p controller/binary
rm -rf controller/binary/ngrinder*
cp ../ngrinder-controller/build/libs/*.war controller/binary/
