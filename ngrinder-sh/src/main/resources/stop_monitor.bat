@ECHO OFF
SET basedir=%~dp0
CD %basedir%
java  -Dstart.mode=stopmonitor -jar ngrinder-core-${ngrinder.version}.jar -server