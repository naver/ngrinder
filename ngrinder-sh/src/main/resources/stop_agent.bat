@ECHO OFF
SET basedir=%~dp0
CD %basedir%
java  -Dstart.mode=stopagent -jar ngrinder-core-${ngrinder.version}.jar -server