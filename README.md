# Complete Selenium Java Framework With Smart Report

A production-shaped automation framework built with Selenium, Java, and TestNG using the Page Object Model pattern. It combines UI testing, API checks, and reusable utilities with separate reporting paths for UI Extent output and API Allure output.

Recent updates added browser-matrix CI execution, runtime browser overrides for local and pipeline runs, and class-level parallel execution for faster suite feedback.

Dockerized browser execution is now available through Selenium standalone containers, so the suite can run against Chrome or Firefox without local browser installs.

The Fire Insurance flow was also stabilized by replacing the hover plus scroll sequence with a direct hover-and-click action on the submenu item, which removed the intermittent timeout on that test.

Built to be discussed in interviews: the architecture mirrors a real enterprise framework and each layer has a clear responsibility.

---

## Tech Stack

| Layer | Choice | Why |
|---|---|---|
| Language | Java 25 | Strong typing, ecosystem maturity, enterprise adoption |
| Automation | Selenium 4.45 | Stable browser automation with broad compatibility |
| Test Runner | TestNG 7.10 | Suites, data providers, grouping, parallel execution |
| Build | Maven | Dependency management and CI-friendly execution |
| Reporting | ExtentReports 5 + Allure | UI Extent HTML plus API Allure attachments and hierarchy |
| Logging | Log4j2 | Structured logs for debugging and auditability |
| Test Data | Apache POI | Data-driven testing through Excel input |

---

## Project Structure

```text
V2/
├── README.md
└── v1/
	├── pom.xml
	├── testng.xml
	├── config/
	│   └── config.properties
	├── src/
	│   ├── main/java/utils/
	│   │   ├── Base.java
	│   │   ├── Reporter.java
	│   │   ├── ApiHelper.java
	│   │   └── ApiReportHelper.java
	│   └── test/java/
	│       ├── base/
	│       ├── Pages/
	│       ├── tests/
	│       └── uistore/
	├── reports/
	└── test-output/
```

Golden rule this structure enforces: test classes focus on behavior, while selectors and page interactions live in page and locator layers.

---

## How To Run

From the v1 folder:

```bash
cd v1
```

Run complete suite:

```bash
mvn clean test
```

If Maven is not installed globally on Windows, use the wrapper:

```bash
mvnw.cmd clean test
```

Run a specific TestNG suite file (already configured in Surefire):

```bash
mvn test
```

---

## Reporting And Outputs

- UI and broken-link Extent reports are generated in the reports folder.
- API tests write sanitized request, response, headers, timings, and assertion steps to `target/allure-results`.
- API Allure results are organized with a cleaner hierarchy so the report reads as API -> Notes API -> endpoint group -> scenario.
- The shared WebDriver layer supports Chrome, Edge, and Firefox, with browser and headless settings overridable from the command line for CI runs.
- TestNG executes classes in parallel so independent smoke checks can run faster without changing test code.
- Generate the API report after an API test run:

```bash
cd v1
mvnw.cmd allure:report
```

The rendered report is available at `target/site/allure-maven-plugin/index.html`.
- TestNG default outputs are generated in test-output.
- Surefire execution artifacts are available under target/surefire-reports.

---

## Design Decisions (Interview Talking Points)

### 1) Layered Page Object Model
Page actions, locators, and tests are separated so UI changes are isolated to the right layer instead of scattered across tests.

### 2) Shared Base Test And Utilities
Cross-cutting setup, driver behavior, reporting, and logging are centralized in base and util classes to reduce duplication and keep tests readable.

### 3) Config-Driven Execution
Environment values are externalized through config.properties so the same codebase runs across local and CI environments with minimal changes.

### 4) Mixed UI + API Validation
The suite validates both browser workflows and API behavior in one framework, which improves confidence in end-to-end quality.

### 5) Data-Driven Expansion
Utility support for Excel-driven inputs makes it easy to scale coverage by adding data sets rather than duplicating test logic.

---

## Current Coverage Snapshot

- Smoke scenarios for baseline health checks.
- UI flows such as Fire Insurance journey validations.
- Broken link detection.
- API checks for Notes authentication and health scenarios.

Recent UI fix: the Fire Insurance menu path now uses a stable hover-and-click helper so the submenu does not collapse before selection.

---

## Why This Framework Shape Works

- Familiar to Selenium/TestNG teams.
- Easy onboarding for new contributors.
- Scales from small test packs to CI pipelines.
- Keeps debugging practical through clear logs and report artifacts.

---

## Roadmap

- Add CI workflow enhancements for scheduled and pull-request runs.
- Introduce richer trend reporting and historical dashboards.
- Increase negative and boundary API/UI datasets.

## CI

The repository includes a GitHub Actions workflow in [v1/.github/workflows/ci.yml](v1/.github/workflows/ci.yml) that runs the suite in a browser matrix across Chrome and Firefox with headless execution.

The workflow passes `browser` and `headless` as Maven system properties so the same codebase can run locally or in CI without editing `config.properties`.

## Docker

Start the browser containers from the `v1` folder:

```bash
docker compose -f docker-compose.selenium.yml up -d
```

Run the suite against the Chrome container:

```bash
mvnw.cmd clean test -Dremote=true -Dbrowser=chrome -Dheadless=false -Dremote.chrome.url=http://localhost:4444
```

Run the suite against the Firefox container:

```bash
mvnw.cmd clean test -Dremote=true -Dbrowser=firefox -Dheadless=false -Dremote.firefox.url=http://localhost:4445
```

Stop the containers when you are done:

```bash
docker compose -f docker-compose.selenium.yml down
```

---

## Change Log

See [change log.md](change%20log.md) for the latest reporting updates and API Allure migration notes.
