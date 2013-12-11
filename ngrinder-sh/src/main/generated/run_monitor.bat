@ECHO OFF
SET basedir=%~dp0
CD %basedir%
java -Dstart.mode=monitor -server -cp "lib/*" org.ngrinder.NGrinderStarter
