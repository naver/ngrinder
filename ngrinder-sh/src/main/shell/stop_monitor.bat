@ECHO OFF
SET basedir=%~dp0
CD %basedir%
java -Dstart.mode=stopmonitor -server -cp "lib/*" org.ngrinder.NGrinderStarter