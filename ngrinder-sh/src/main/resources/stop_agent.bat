@ECHO OFF
SET basedir=%~dp0
CD %basedir%
java -Dstart.mode=stopagent -server -cp "lib/*" org.ngrinder.NGrinderStarter