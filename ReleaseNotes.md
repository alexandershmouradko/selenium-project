# Release notes

## 3.0.0-SNAPSHOT

- migrated build to Gradle 8.14.5 and Java 17 bytecode
- migrated Cucumber 1 to Cucumber 7 and TestNG 7
- migrated Selenium 3 to Selenium 4 with W3C capabilities and Selenium Manager
- migrated REST Assured, WireMock, MongoDB, PDFBox, POI and logging libraries
- replaced global execution state with thread-confined context
- replaced obsolete Extent internals with the public Cucumber Scenario reporting API
- added sources and Javadoc artifacts for every module
- added framework regression tests plus API, WireMock and capability smoke tests
- added standalone published-artifact consumer verification
- removed committed repository credentials and obsolete browser drivers
- documented unsupported legacy integrations instead of silently emulating them
