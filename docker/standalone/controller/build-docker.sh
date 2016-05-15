#!/usr/bin/env bash
mkdir -p binary
cp ../../../../ngrinder-controller/target/*.war binary/
docker build -t ngrinder-controller .