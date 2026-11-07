@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\bootstrap-maven.ps1" %*
exit /b %ERRORLEVEL%
