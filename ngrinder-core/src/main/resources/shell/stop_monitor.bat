@echo off
For /F "tokens=1* delims==" %%A IN (agent_pid.conf) DO (
    IF "%%A"=="monitor.pid" set Monitor_Pid=%%B
)
taskkill /pid %Monitor_Pid% /f
