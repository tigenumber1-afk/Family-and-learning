#!/bin/sh
# Gradle Wrapper launcher placeholder: official gradle-wrapper.jar must be generated/added.
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$JAR" ]; then
  echo "ERROR: gradle/wrapper/gradle-wrapper.jar is missing." >&2
  echo "Generate it with Gradle 9.6.0 using: gradle wrapper --gradle-version 9.6.0" >&2
  exit 1
fi
exec java -jar "$JAR" "$@"
