# Jelly

## Module structure
- `ide:app` - editor UI
- `jcc` - compiler core
- `perf-check` - runs a simple performance measure on map/reduce
- `utils` - common helpers
- `viewer` - simple tool to render parse trees

## Running the app
Just navigate to the project folder and execute in the Terminal:
```
./gradlew :ide:app:run
```

## Frameworks/Libraries used
- ANTLR for generating the lexer/parser/parse tree
- Compose Desktop for the editor UI
- Kotlin Coroutines to organize concurrency
- JUnit, kotest & mmock for unit testing

Full list of dependencies can be found in `gradle/libs.versions.toml`
