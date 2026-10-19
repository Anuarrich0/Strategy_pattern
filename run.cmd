@echo off
chcp 65001 >nul
cd /d "%~dp0"
call mvn -q -DskipTests package
if errorlevel 1 exit /b %errorlevel%
java -Dfile.encoding=UTF-8 -jar target\taxi-strategy-1.0.0.jar %*
