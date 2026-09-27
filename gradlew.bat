@ECHO OFF
SETLOCAL
SET APP_HOME=%~dp0
SET JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
SET JAR_URL=https://raw.githubusercontent.com/gradle/gradle/v9.6.0/gradle/wrapper/gradle-wrapper.jar

IF EXIST "%JAR%" GOTO RUN_WRAPPER

ECHO Gradle Wrapper JAR is missing; downloading the official Gradle 9.6.0 Wrapper JAR...
where powershell >NUL 2>&1
IF ERRORLEVEL 1 (
  ECHO ERROR: PowerShell is required to bootstrap the Gradle Wrapper.
  EXIT /B 1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri '%JAR_URL%' -OutFile '%JAR%.tmp'"
IF ERRORLEVEL 1 EXIT /B 1

powershell -NoProfile -ExecutionPolicy Bypass -Command "$h=(Get-FileHash -Algorithm SHA256 '%JAR%.tmp').Hash.ToLower(); if($h -ne '497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7'){exit 1}"
IF ERRORLEVEL 1 (
  ECHO ERROR: downloaded Gradle Wrapper JAR checksum mismatch.
  DEL /Q "%JAR%.tmp" >NUL 2>&1
  EXIT /B 1
)

MOVE /Y "%JAR%.tmp" "%JAR%" >NUL
IF ERRORLEVEL 1 EXIT /B 1

:RUN_WRAPPER
java -classpath "%JAR%" org.gradle.wrapper.GradleWrapperMain %*
ENDLOCAL
EXIT /B %ERRORLEVEL%
