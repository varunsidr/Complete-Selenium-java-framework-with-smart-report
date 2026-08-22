# Change Log

## 2026-08-23 Delivery and docs updates

- Added a working CI badge and live GitHub Pages report link to the README.
- Added a GitHub Pages workflow to publish the latest Extent and Allure reports.
- Added a Jenkinsfile so the same browser flow can be run and archived from Jenkins.
- Expanded the README with the reasoning behind ThreadLocal, explicit waits, composition, and failure propagation.

## 2026-08-23 Fire Insurance fix

- Stabilized the Fire Insurance hover flow by replacing scroll-plus-JS click with a single hover-and-click action.
- Added a reusable hover-and-click helper to keep hover-revealed menus from collapsing during selection.
- Confirmed the Fire Insurance test passes after the fix.

## 2026-08-22 Docker support

- Added remote WebDriver support to the shared driver factory.
- Added Selenium standalone Chrome and Firefox containers through Docker Compose.
- Added configuration values for local and remote browser execution.
- Documented the Docker browser workflow in the README.

## 2026-08-22

- Added Firefox support to the shared WebDriver factory.
- Enabled class-level parallel execution in TestNG for faster suite runs.
- Added runtime browser, headless, and URL overrides through Maven system properties.
- Updated the GitHub Actions workflow to run a browser matrix across Chrome and Firefox.
- Documented the CI browser matrix and execution overrides in the README.

## 2026-08-21

- Moved API reporting from Extent HTML to Allure attachments and results.
- Added Allure TestNG support for the API test suite only.
- Kept UI and broken-link reporting on ExtentReports.
- Added cleaner Allure hierarchy for API tests so the report reads as API -> Notes API -> endpoint group -> scenario.
- Documented the API Allure output path and report generation steps in the README.
- Verified the API suite passes after the reporting changes.