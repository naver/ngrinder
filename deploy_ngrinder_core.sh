#!/bin/bash

mvnw -pl ngrinder-core -am  -Pjavadoc  -Dmaven.test.skip=true clean package deploy
