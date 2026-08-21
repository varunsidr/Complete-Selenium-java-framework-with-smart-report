# Change Log

## 2026-08-21

- Moved API reporting from Extent HTML to Allure attachments and results.
- Added Allure TestNG support for the API test suite only.
- Kept UI and broken-link reporting on ExtentReports.
- Added cleaner Allure hierarchy for API tests so the report reads as API -> Notes API -> endpoint group -> scenario.
- Documented the API Allure output path and report generation steps in the README.
- Verified the API suite passes after the reporting changes.