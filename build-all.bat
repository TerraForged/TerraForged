echo off

echo Building...

(
  start "Forge Builder" build-forge.bat
  start "Fabric Builder" build-fabric.bat
) | set /P "="

echo Copying jars...
mkdir jars
xcopy /s /d /y build\libs\ jars
xcopy /s /d /y Fabric\build\libs\ jars

echo Done!
explorer jars