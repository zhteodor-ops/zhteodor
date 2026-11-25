@echo off
title Building Jerry Mod...
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
  echo.
  echo  Java was not found on this PC.
  echo  Install Java 17 from https://adoptium.net  ^(choose "Temurin 17", Windows .msi^)
  echo  Then double-click build.bat again.
  echo.
  pause
  exit /b 1
)
echo Building Jerry Mod for Minecraft Forge 1.20.1 ...
echo The first build downloads Minecraft and Forge files, so it can take 5-15 minutes.
call gradlew.bat build
if errorlevel 1 (
  echo.
  echo  BUILD FAILED - copy the red error text above and send it to Claude.
  pause
  exit /b 1
)
copy /Y build\libs\jerrymod-1.0.0.jar jerrymod-1.0.0.jar >nul
echo.
echo  DONE!  jerrymod-1.0.0.jar is in this folder.
echo  Put it in:  %%APPDATA%%\.minecraft\mods
echo.
explorer .
pause
