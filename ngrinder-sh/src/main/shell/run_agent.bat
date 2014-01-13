@ECHO OFF
SET basedir=%~dp0
CD %basedir%

:RUN
IF EXIST  .\update_package\lib (
	@ECHO ON
	REM update package and run
	RMDIR /S /Q .\lib
	@ECHO UPDATE NGRINDER_AGENT
	@ECHO OFF
	XCOPY /E /Q /Y .\update_package .\
	RMDIR /S /Q .\update_package
)

CALL .\run_agent_internal.bat %*

IF NOT EXIST  .\update_package\lib (
	GOTO END
)
GOTO RUN

:END
