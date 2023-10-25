# Jelly

## Module structure
- `ide:desktopApp` - editor UI for Desktop (Mac, Win, Linux)
- `ide:androidApp` - editor UI for Android
- `ide:shared` - cross-platform code for the editor
- `base:logging` - internal logging framework
- `base:utils` - common helpers
- `jcc:core` - compiler core
- `jcc:perf-check` - runs a simple performance measure on map/reduce
- `jcc:viewer` - simple tool to render parse trees

## Prerequisites
- Java 17 (Please don't use the one from Homebrew - you won't be able to build the distribution package!). I suggest using Amazon Coretto.
- In order to build a distribution package on Ubuntu, you need a `fakeroot` package. You can install it by running `sudo apt-get install fakeroot`.

## Running the app on Desktop
Just navigate to the project folder and execute in the Terminal:
```
./gradlew :ide:desktopApp:run
```

## Building the Desktop distribution

Debug build: `./gradlew packageDistributionForCurrentOS`

Release build: `./gradlew packageReleaseDistributionForCurrentOS -Penv=release`

## Running the app on Android
Just navigate to the project folder and execute in the Terminal:
```
./gradlew :ide:androidApp:installDebug
```
And open the "Jelly" app on the device afterward.

## Frameworks/Libraries used
- ANTLR for generating the lexer/parser/parse tree
- Compose Desktop for the editor UI
- Kotlin Coroutines to organize concurrency
- Apache's FastMath for exponentiation
- Okio for file operations
- AndroidX for Android app and annotations
- Detekt for formatting & simple static analysis
- JUnit, kotest & mockk for unit testing

Full list of dependencies can be found in `gradle/libs.versions.toml`
