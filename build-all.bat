echo off

echo Generating Resources...
call gradlew clearData runData

echo Building Mod Jars...
(
  start "Forge Builder" build-forge.bat
  start "Fabric Builder" build-fabric.bat
) | set /P "="

echo Collecting Mod Jars...
rmdir /s /q jars
mkdir jars
xcopy /s /d /y build\libs\ jars
xcopy /s /d /y Fabric\build\libs\ jars

echo Done!
explorer jars