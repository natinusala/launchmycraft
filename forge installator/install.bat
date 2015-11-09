@echo off
rem #=======================================================================#
rem #	Générateur d'archive Forge - LaunchMyCraft							#
rem #																		#
rem #	Description	: Génération d'une archive Minecraft avec				#
rem #				  Forge pour l'intégration avec le launcher				#
rem #				  LaunchMyCraft											#
rem #	Auteur 		: Natinusala - forum.launchmycraft.fr					#
rem #	Version		: 2.3													#
rem #	Changelog	:														#
rem #	- 21/06/2015 - 2.0 - Apokalysme - Gestion des versions Forge		#
rem #									10.13.4 et supérieures.				#
rem #	- 25/06/2015 - 2.1 - Apokalysme - Gestion des versions Forge		#
rem #									pour Minecraft 1.8.					#
rem #	- 29/06/2015 - 2.2 - Apokalysme - Complément de la 2.0 pour Forge	#
rem #									10.13.3								#
rem #									- Affichage de la version			#
rem #	- 10/07/2015 - 2.3 - Apokalysme - Génération d'un log				#
rem #									- Protection de l'exécution depuis	#
rem #									l'archive ZIP						#
rem #=======================================================================#

rem ----- Création d'un dossier dans %appdata% pour loguer même dans le cas
rem ----- d'une exécution depuis l'archive ZIP
if not exist %appdata%\LaunchMyCraftGenerator\nul mkdir %appdata%\LaunchMyCraftGenerator
set GEN_LOGFILE=%appdata%\LaunchMyCraftGenerator\generator.log

rem ----- Début du script
echo == Préparateur d'archives Forge pour LaunchMyCraft - Version 2.3 ==
echo == Préparateur d'archives Forge pour LaunchMyCraft - Version 2.3 == > %GEN_LOGFILE%

rem ----- Vérification du contenu du dossier courant
echo Dossier courant : %CD%  >> %GEN_LOGFILE%
if not exist generated_files\nul goto ERRORCWD
if not exist output\nul goto ERRORCWD
if not exist tools\nul goto NOTOOLS
if not exist installer.jar goto NOINSTALLER

rem ----- Récupération des versions de Minecraft et de Forge
echo Entrez la version de Minecraft correspondant à Forge : | tools\tee\tee.exe -a %GEN_LOGFILE%
set /p MinecraftName=
echo Valeur entrée pour Minecraft : %MinecraftName% >> %GEN_LOGFILE%
echo Entrez la version de Forge à installer : | tools\tee\tee.exe -a %GEN_LOGFILE%
set /p ForgeName=
echo Valeur entrée pour Forge : %ForgeName% >> %GEN_LOGFILE%

rem ----- Nettoyage en cas d'énième exécution du programme
echo == Nettoyage des dossiers... | tools\tee\tee.exe -a %GEN_LOGFILE%
cd /d .\generated_files
if %errorlevel% NEQ 0 goto ERRORCWD
for /F "delims=" %%i in ('dir /b') do (rmdir "%%i" /s/q || del "%%i" /s/q)
cd..
cd /d .\output
if %errorlevel% NEQ 0 goto ERRORCWD
for /F "delims=" %%i in ('dir /b') do (rmdir "%%i" /s/q || del "%%i" /s/q)
cd..

rem ----- Préparation des dossiers utilisés pour la génération
echo == Préparation des dossiers... | tools\tee\tee.exe -a %GEN_LOGFILE%
mkdir ".\output"
mkdir ".\generated_files\mods"
echo Mettez vos mods ici ! >".\generated_files\mods\README.txt"
mkdir ".\generated_files\libraries"
mkdir ".\generated_files\versions\%MinecraftName%\"

rem ----- Récupération dans le dossier d'installation Officiel de Minecraft
echo == Copie du fichier des profils... | tools\tee\tee.exe -a %GEN_LOGFILE%
xcopy /y "%appdata%\.minecraft\launcher_profiles.json" ".\generated_files\*" /Q
echo Copie de Minecraft %MinecraftName%... | tools\tee\tee.exe -a %GEN_LOGFILE%
xcopy /Q "%appdata%\.minecraft\versions\%MinecraftName%" ".\generated_files\versions\%MinecraftName%"

rem ----- Lancement de l'installation de Forge
echo == Exécution de l'installateur... | tools\tee\tee.exe -a %GEN_LOGFILE%
echo Installez le client dans le dossier donné | tools\tee\tee.exe -a %GEN_LOGFILE%
echo Dossier à choisir : | tools\tee\tee.exe -a %GEN_LOGFILE%
echo %CD%\generated_files | tools\tee\tee.exe -a %GEN_LOGFILE%
pause
java -jar installer.jar

rem ----- Récupération du log de Forge dans le log général du programme
type installer.jar.log >> %GEN_LOGFILE%
del installer.jar.log /Q

rem ----- Traitement avant création du ZIP
echo == Finalisation du dossier... | tools\tee\tee.exe -a %GEN_LOGFILE%
for /f "delims=. tokens=1" %%a IN ("%ForgeName%") DO set FMAJOR=%%a
for /f "delims=. tokens=2" %%b IN ("%ForgeName%") DO set fminor=%%b
for /f "delims=. tokens=3" %%c IN ("%ForgeName%") DO set fpatch=%%c

if %FMAJOR% EQU 10 if %fminor% GEQ 13 if %fpatch% GEQ 3 goto POST17
if %FMAJOR% GEQ 11 goto POST18

rem ----- Cas des versions 10.13.2 et plus anciennes
echo == Forge Version 10.13.2 et plus anciennes | tools\tee\tee.exe -a %GEN_LOGFILE%
del ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json" /Q
copy ".\generated_files\versions\%MinecraftName%-Forge%ForgeName%\%MinecraftName%-Forge%ForgeName%.json" ".\generated_files\versions\%MinecraftName%"
rename ".\generated_files\versions\%MinecraftName%\%MinecraftName%-Forge%ForgeName%.json" "%MinecraftName%.json"
rmdir /s /q ".\generated_files\versions\%MinecraftName%-Forge%ForgeName%"
del ".\generated_files\launcher_profiles.json" /Q
goto ZIP

:POST17
rem ----- Cas des versions 10.13.3 et supérieures
echo == Forge Version 10.13.3 et supérieures | tools\tee\tee.exe -a %GEN_LOGFILE%
mkdir ".\generated_files\versions\release"
tools\sed\sed.exe -e "s/id\".*/id\":\ \"release\",/" -i ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json"
move ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json" ".\generated_files\versions\release"
rename ".\generated_files\versions\release\%MinecraftName%.json" "release.json"
copy ".\generated_files\versions\%MinecraftName%-Forge%ForgeName%-%MinecraftName%\%MinecraftName%-Forge%ForgeName%-%MinecraftName%.json" ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json"
tools\sed\sed.exe -e "s/id\".*/id\":\ \"%MinecraftName%\",/" -i ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json"
tools\sed\sed.exe -e "s/inheritsFrom.*/inheritsFrom\":\ \"release\",/" -i ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json"
rmdir /s /q ".\generated_files\versions\%MinecraftName%-Forge%ForgeName%-%MinecraftName%"
del ".\generated_files\launcher_profiles.json" /Q
goto ZIP

:POST18
rem ----- Cas des versions 11 et supérieures
echo == Forge Version 11 et supérieures | tools\tee\tee.exe -a %GEN_LOGFILE%
mkdir ".\generated_files\versions\release"
tools\sed\sed.exe -e "s/id\".*/id\":\ \"release\",/" -i ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json"
move ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json" ".\generated_files\versions\release"
rename ".\generated_files\versions\release\%MinecraftName%.json" "release.json"
copy ".\generated_files\versions\%MinecraftName%-Forge%ForgeName%\%MinecraftName%-Forge%ForgeName%.json" ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json"
tools\sed\sed.exe -e "s/id\".*/id\":\ \"%MinecraftName%\",/" -i ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json"
tools\sed\sed.exe -e "s/inheritsFrom.*/inheritsFrom\":\ \"release\",/" -i ".\generated_files\versions\%MinecraftName%\%MinecraftName%.json"
rmdir /s /q ".\generated_files\versions\%MinecraftName%-Forge%ForgeName%"
del ".\generated_files\launcher_profiles.json" /Q
goto ZIP

:ZIP
echo == Préparation de l'archive... | tools\tee\tee.exe -a %GEN_LOGFILE%
cscript //nologo zip.vbs  "%CD%\generated_files\versions" "%CD%\output\%MinecraftName%-forge-%ForgeName%.zip"
cscript //nologo zip.vbs  "%CD%\generated_files\libraries" "%CD%\output\%MinecraftName%-forge-%ForgeName%.zip"
cscript //nologo zip.vbs  "%CD%\generated_files\mods" "%CD%\output\%MinecraftName%-forge-%ForgeName%.zip"

del sed* /Q
echo == Terminé - %MinecraftName%-forge-%ForgeName%.zip créé ! | tools\tee\tee.exe -a %GEN_LOGFILE%
goto END

:NOINSTALLER
echo ERREUR - Le fichier installer.jar n'est pas présent dans le dossier >> %GEN_LOGFILE%
goto END

:NOTOOLS
echo ERREUR - Les ne sont pas présents dans le dossier >> %GEN_LOGFILE%
goto END

:ERRORCWD
echo ERREUR - Le programme n'est pas décompressé ou le lancement n'est pas fait depuis le bon endroit >> %GEN_LOGFILE%
goto END

:END
pause
