#!/bin/sh

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd) || exit 1
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
JAR_URL="https://raw.githubusercontent.com/gradle/gradle/v9.6.0/gradle/wrapper/gradle-wrapper.jar"
JAR_SHA256="497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7"

if [ ! -f "$JAR" ]; then
  echo "Gradle Wrapper JAR is missing; downloading the official Gradle 9.6.0 Wrapper JAR..." >&2
  TMP_JAR="$JAR.tmp"
  rm -f "$TMP_JAR"
  if command -v curl >/dev/null 2>&1; then
    curl --fail --location --retry 3 --output "$TMP_JAR" "$JAR_URL" || exit 1
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$TMP_JAR" "$JAR_URL" || exit 1
  else
    echo "ERROR: curl or wget is required to bootstrap the Gradle Wrapper." >&2
    exit 1
  fi
  if command -v sha256sum >/dev/null 2>&1; then
    ACTUAL=$(sha256sum "$TMP_JAR" | awk '{print $1}')
  elif command -v shasum >/dev/null 2>&1; then
    ACTUAL=$(shasum -a 256 "$TMP_JAR" | awk '{print $1}')
  else
    echo "ERROR: no SHA-256 utility is available." >&2
    rm -f "$TMP_JAR"
    exit 1
  fi
  if [ "$ACTUAL" != "$JAR_SHA256" ]; then
    echo "ERROR: downloaded Gradle Wrapper JAR checksum mismatch." >&2
    rm -f "$TMP_JAR"
    exit 1
  fi
  mv "$TMP_JAR" "$JAR"
fi

exec java -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
