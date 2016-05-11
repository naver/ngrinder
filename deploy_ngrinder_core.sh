#!/bin/bash

mvn -pl ngrinder-core -am -DaltDeploymentRepository=release-repo::default::file:../ngrinder.maven.repo/releases -Dmaven.test.skip=true clean package deploy
