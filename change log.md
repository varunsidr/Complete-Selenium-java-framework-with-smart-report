# Change Log

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