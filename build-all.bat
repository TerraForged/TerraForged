echo off

echo Clearing Old Resources...
rmdir /s /q jars
rmdir /s /q generated\resources\

echo Generating Resources...
call gradlew runData

echo Building Mod Jars...
(
  start "Forge Builder" build-forge.bat
  start "Fabric Builder" build-fabric.bat
) | set /P "="

echo Collecting Mod Jars...
mkdir jars
xcopy /s /d /y build\libs\ jars
xcopy /s /d /y Fabric\build\libs\ jars

echo Done!
explorer jars