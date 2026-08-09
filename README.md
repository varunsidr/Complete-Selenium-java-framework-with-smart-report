# Complete Selenium Java Framework With Smart Report

A hybrid Selenium + TestNG automation framework with smart reporting, API helpers, logging support, and data-driven utilities.

## Project Structure

- `v1/`: main Maven project
- `v1/src/test/java/tests/`: UI and API test classes
- `v1/config/config.properties`: runtime configuration
- `v1/testng.xml`: TestNG suite configuration
- `v1/reports/` and `v1/test-output/`: generated test reports

## Tech Stack

- Java 25
- Maven
- Selenium 4
- TestNG
- ExtentReports
- Apache POI
- Log4j2

## Run Locally

1. Open a terminal in `v1`.
2. Run tests:

```bash
mvn clean test
```

If you are on Windows and Maven is not installed globally, use the wrapper:

```bash
./mvnw.cmd clean test
```

## Notes

- The suite file is configured in Maven Surefire as `testng.xml`.
- Update `v1/config/config.properties` before running tests in a new environment.
