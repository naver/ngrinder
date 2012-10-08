@ECHO OFF
SET basedir=%~dp0
CD %basedir%
java  -Dstart.mode=agent -jar ngrinder-core-${ngrinder.version}.jar -server