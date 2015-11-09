@echo off
echo == Pr‚parateur d'archives Forge pour LaunchMyCraft ==
echo Entrez la version de Minecraft dont correspond le Forge : 
set /p MinecraftName=
echo Entrez la version de Forge … installer :
set /p ForgeName=
cls
echo == Nettoyage des dossiers...
cd /d .\generated_files
for /F "delims=" %%i in ('dir /b') do (rmdir "%%i" /s/q || del "%%i" /s/q)
cd..
cd /d .\output
for /F "delims=" %%i in ('dir /b') do (rmdir "%%i" /s/q || del "%%i" /s/q)
cd..
mkdir ".\output"
cls
echo == Pr‚paration du dossier...
mkdir ".\generated_files\mods"
echo Mettez vos mods ici ! >".\generated_files\mods\README.txt"
mkdir ".\generated_files\libraries"
mkdir ".\generated_files\versions\%MinecraftName%\"
cls
echo == Copie du fichier des profils...
xcopy /y "%appdata%\.minecraft\launcher_profiles.json" ".\generated_files\*" /Q
echo Copie de Minecraft %MinecraftName%...
xcopy /Q "%appdata%\.minecraft\versions\%MinecraftName%" ".\generated_files\versions\%MinecraftName%"
cls
echo == Ex‚cution de l'installateur...
echo Installez le client dans le dossier donn‚
echo Dossier … choisir : 
echo %CD%\generated_files
pause Appuyez pour lancer l'installateur...
java -jar installer.jar
cls
echo == Finalisation du dossier...
del ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json" /Q
copy ".\generated_files\versions\%MinecraftName%-Forge%ForgeName%\%MinecraftName%-Forge%ForgeName%.json" ".\generated_files\versions\%MinecraftName%"
rename ".\generated_files\versions\%MinecraftName%\%MinecraftName%-Forge%ForgeName%.json" "%MinecraftName%.json"
rmdir /s /q ".\generated_files\versions\%MinecraftName%-Forge%ForgeName%"
del ".\generated_files\launcher_profiles.json" /Q
cls
echo == Pr‚paration de l'archive...
CScript zip.vbs "%CD%\generated_files" "%CD%\output\%MinecraftName%-forge-%ForgeName%.zip"
cls
echo == Termin‚ - %MinecraftName%-forge-%ForgeName%.zip cr‚‚ !
pause
