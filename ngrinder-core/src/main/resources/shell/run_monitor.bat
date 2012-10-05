@ECHO OFF
SET basedir=%~dp0
CD %basedir%
java -jar  -Dstart.mode=monitor ngrinder-core-${ngrinder.version}.jar -server