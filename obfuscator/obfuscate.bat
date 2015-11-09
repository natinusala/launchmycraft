@echo off

echo Nettoyage...
del /F /Q .\obfuscated\*.*

IF EXIST "%PROGUARD_HOME%" GOTO home

SET PROGUARD_HOME=..

:home

echo Obfuscation du bootstrap...
java -jar ".\proguard\lib\proguard.jar" @config\bootstrapconfig.pro

echo Obfuscation du launcher...
java -jar ".\proguard\lib\proguard.jar" @config\launcherconfig.pro

echo Terminé !
pause