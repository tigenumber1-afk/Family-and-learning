@ECHO OFF
SET APP_HOME=%~dp0
SET JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
IF NOT EXIST "%JAR%" (
  ECHO ERROR: gradle\wrapper\gradle-wrapper.jar is missing.
  ECHO Generate it with Gradle 9.6.0 using: gradle wrapper --gradle-version 9.6.0
  EXIT /B 1
)
java -jar "%JAR%" %*
