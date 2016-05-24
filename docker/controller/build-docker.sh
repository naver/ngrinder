#!/usr/bin/env bash
mkdir -p binary
rm binary/*
cp ../../ngrinder-controller/target/*.war binary/
docker build -t ngrinder-controller .