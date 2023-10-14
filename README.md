# Jelly

## Module structure
- `ide:app` - editor UI
- `jcc:core` - compiler core
- `jcc:perf-check` - runs a simple performance measure on map/reduce
- `jcc:viewer` - simple tool to render parse trees
- `utils` - common helpers

## Running the app
Just navigate to the project folder and execute in the Terminal:
```
./gradlew :ide:app:run
```

## Frameworks/Libraries used
- ANTLR for generating the lexer/parser/parse tree
- Compose Desktop for the editor UI
- Kotlin Coroutines to organize concurrency
- Apache's FastMath for expontiation
- Okio for fire operations
- AndroidX annotations for code checks
- JUnit, kotest & mockk for unit testing

Full list of dependencies can be found in `gradle/libs.versions.toml`
