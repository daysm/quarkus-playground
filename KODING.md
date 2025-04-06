# KODING.md - Commands and Guidelines

## Build & Run Commands
- Build application: `./gradlew build`
- Run in dev mode: `./gradlew quarkusDev`
- Run all tests: `./gradlew test`
- Run single test: `./gradlew test --tests "dev.dayyan.BookResourceTest.testGetBookFound"`
- Format code: `./gradlew ktlintFormat`
- Check lint: `./gradlew ktlintCheck`

## Code Style Guidelines
- Kotlin conventions with 4 space indentation
- Import statements should be organized and unused imports removed
- All properties/fields should be explicitly typed
- Use val over var when possible
- Class naming: PascalCase (e.g., BookResource)
- Functions/properties: camelCase (e.g., getBook)
- Error handling: Use nullable types with conditional responses as shown in BookResource
- Each class should have a single responsibility
- Prefer constructor injection over field injection in non-test code
- Use @QuarkusTest annotation for integration tests
- Follow existing PostgresTestBase pattern for database tests