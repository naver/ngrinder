@ECHO OFF
SET CONTROLLER_IP=
IF "%1"=="" GOTO RUN
   SET CONTROLLER_IP=-Dcontroller=%1
:RUN
java  -Dstart.mode=agent %CONTROLLER_IP% -cp "lib/*" org.ngrinder.NGrinderStarter -server
