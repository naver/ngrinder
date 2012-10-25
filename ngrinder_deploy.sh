#!/bin/bash

git pull

rm -Rf ngrinder-core/target/*
rm -Rf ngrinder-controller/target/*
rm -Rf ngrinder-dns/target/*


if [[ ! -e ../nhnopensource.maven.repo ]]; then
            cd ..
	    git clone https://github.com/nhnopensource/nhnopensource.maven.repo.git 
	    cd ngrinder
fi
mvn -Dmaven.test.skip=true deploy

cd ../nhnopensource.maven.repo
echo "Enter user.email:"
read USERMAIL
echo "Enter user.name:"
read USERNAME

git add *
git config --global user.email $USERMAIL
git config --global user.name  $USERNAME
git commit -a -m "upload nGrinder jar"
git push --all
