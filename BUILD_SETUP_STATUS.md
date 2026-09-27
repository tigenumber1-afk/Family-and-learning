# Build setup status

- Android Gradle Plugin: 9.4.0
- Gradle Wrapper target: 9.6.0
- JDK target: 17+
- compileSdk: 36
- targetSdk: 36

The original project did not contain a Gradle Wrapper.
The build configuration has been standardized so the Android Gradle Plugin version is explicit and the intended Gradle version is known.

The official `gradle-wrapper.jar` could not be generated in the current execution environment because no Gradle distribution is installed locally and external binary downloads are blocked. No unofficial or fabricated wrapper JAR was inserted.
