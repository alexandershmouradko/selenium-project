# Cukes Automation Framework 3

Modernized multi-module Java test automation framework based on the original Cucumber/TestNG framework.

The project preserves the original framework concepts—modular JARs, Cucumber scenarios, TestNG execution, browser matrices, API helpers, DB helpers, mocking and CI integration—while replacing obsolete libraries and unsafe runtime patterns.

## Current baseline

- Java target: 17
- Gradle wrapper: 8.14.5
- Cucumber JVM: 7.33.0
- TestNG: 7.12.0
- Selenium: 4.44.0
- REST Assured: 5.5.7
- WireMock: 3.13.2
- Log4j: 2.26.0
- MongoDB synchronous driver: 5.5.1
- PDFBox: 3.0.5
- Apache POI: 5.4.1

All Cucumber modules are aligned through the Cucumber BOM.

## Modules

- `cf-common-utils` — Cucumber/TestNG runner, configuration, thread context, reporting facade and general helpers.
- `cf-selenium-utils` — Selenium 4 driver factory, local/Grid/Sauce Labs/BrowserStack providers, waits, page objects and screenshots.
- `cf-api-utils` — REST Assured helpers, reusable Cucumber API steps, JSON/schema/OpenAPI validation.
- `cf-database-utils` — JDBC, MongoDB and version-neutral Elasticsearch REST helpers.
- `cf-mocking-utils` — thread-confined WireMock server lifecycle and reusable steps.
- `cf-jira-utils` — Jira REST integration and execution result hooks.
- `cf-har-utils` — Chrome/Edge performance-log capture for network diagnostics.

## Build and verify

Requirements:

- JDK 17 or newer
- Network access to Maven Central or an enterprise Maven proxy

Run the complete verification:

```bash
./gradlew clean frameworkCheck build sourcesJar javadocJar publishToMavenLocal
```

Run the standalone consumer smoke project after publishing locally:

```bash
./gradlew publishToMavenLocal
./gradlew -p examples/consumer-smoke clean test
```

The convenience script performs both operations:

```bash
./scripts/validate.sh
```

## Use from another Gradle project

```groovy
repositories {
    mavenCentral()
    mavenLocal() // replace with your Nexus/Artifactory repository in CI
}

dependencies {
    testImplementation 'com.acn.ndc:cf-common-utils:3.0.0-SNAPSHOT'
    testImplementation 'com.acn.ndc:cf-selenium-utils:3.0.0-SNAPSHOT'
    testImplementation 'com.acn.ndc:cf-api-utils:3.0.0-SNAPSHOT'
}

test {
    useTestNG()
}
```

Only add the modules required by the consumer project.

## Runtime model

The framework retains the original system-property style, for example:

```bash
./gradlew test \
  -Dcukes.env=local \
  -Dcukes.selenium=true \
  -Dcukes.browsercombo=LOCAL_CH \
  -Dcucumber.filter.tags='@smoke'
```

Secrets must be supplied through environment variables, Jenkins Credentials or protected Gradle user properties. Do not commit usernames, passwords, access keys or tokens.

## Browser support

Supported local baseline:

- Chrome
- Firefox
- Microsoft Edge
- Safari on macOS

Remote providers:

- Selenium Grid
- Sauce Labs using `sauce:options`
- BrowserStack using `bstack:options`

Internet Explorer, PhantomJS and HtmlUnit were removed from the modern baseline. Perfecto remains a compatibility placeholder and intentionally fails with a clear message until an account-specific W3C/Appium provider module is implemented.

Selenium Manager is used automatically unless `-Dcukes.driverPath=...` is explicitly supplied.

## Reporting

The default Cucumber plugins are intentionally dependency-light:

- console `pretty` output
- Cucumber JSON
- Cucumber HTML

The framework reporting facade uses `Scenario.log()` and `Scenario.attach()`. Extent or Allure can be added by a consumer project without coupling the framework core to a specific third-party adapter.

## Important migration notes

Read:

- [`docs/MIGRATION.md`](docs/MIGRATION.md)
- [`validation/VALIDATION_RESULTS.md`](validation/VALIDATION_RESULTS.md)
- [`LEGAL-NOTICE.md`](LEGAL-NOTICE.md)

The original documentation is retained under `docs/legacy/` for historical reference. Commands, versions, URLs and credentials patterns in the legacy documentation must not be treated as current guidance.
