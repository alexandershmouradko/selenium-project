# Validation results

Validation date: July 13, 2026 (America/Los_Angeles)

## Result

The modernized source tree compiled and tested successfully with real external dependencies. All seven framework modules produced main, source and Javadoc JARs and were published to Maven Local. A separate consumer project then resolved and used the published framework artifacts successfully.

## Dependency-resolved build

Executed with Gradle 8.14.5:

```bash
gradle clean frameworkCheck build sourcesJar javadocJar publishToMavenLocal
```

Result:

```text
BUILD SUCCESSFUL
72 actionable tasks
```

The build ran on OpenJDK 21 but compiled every module using `--release 17`. A representative compiled class reports bytecode major version 61, which is Java 17.

## Automated tests

Framework TestNG tests:

```text
tests=15
failures=0
errors=0
skipped=0
```

Covered areas:

- typed properties and escaped list parsing
- system-property precedence and required-variable failure
- thread-local execution context and defensive browser matrix copying
- Cucumber JSON text-report generation
- W3C capabilities and legacy BrowserStack OS aliases
- rejection of unnamespaced custom capabilities
- a real local REST call through the framework REST Assured helper
- fail-fast handling of an unsupported HTTP method
- WireMock startup, dynamic port, stub registration, HTTP response and shutdown

Detailed test names are recorded in `test-summary.txt`.

## Published consumer verification

Executed after `publishToMavenLocal`:

```bash
gradle -p examples/consumer-smoke clean test
```

Result:

```text
BUILD SUCCESSFUL
```

The standalone consumer resolved these modules as external Maven dependencies:

- `cf-common-utils`
- `cf-api-utils`
- `cf-selenium-utils`

## Source artifacts

Every module generated:

- main JAR
- `-sources.jar`
- `-javadoc.jar`

The generated source JARs were compared against every file under each module's `src/main/java` tree:

```text
7 modules
54 Java source files
missing from sources JARs: 0
unexpected source files: 0
```

The supplied legacy common and Selenium 2.1.0 JARs were also compared with the original source tree:

```text
cf-common-utils: missing source for JAR top-level classes = 0
cf-selenium-utils: missing source for JAR top-level classes = 0
```

Therefore decompilation was not required. The relevant evidence is in `original-jar-source-coverage.txt` and `sources-jar-parity.txt`.

## Static migration checks

No active source/build file contains:

- `info.cukes`
- `cucumber.api`
- Commons Configuration 1 imports
- `DesiredCapabilities`
- integer-based Selenium 3 `WebDriverWait` construction
- `jcenter()`
- `uploadArchives`
- `mavenDeployer`
- the removed Gradle `compile` configuration

A credential-pattern scan found no literal passwords, access keys, secrets or tokens in active code or build configuration.

## Gradle Wrapper verification

The included wrapper JAR launched Gradle 8.14.5 successfully using a local copy of the exact distribution identified by the configured SHA-256 checksum:

```text
6f74b601422d6d6fc4e1f9a1ab6522f642c2fdcbc15ae33ebd30ba3d7198e854
```

The final wrapper configuration points to the official Gradle distribution URL and keeps checksum verification enabled. The execution environment used for this modernization could not resolve `services.gradle.org`, so the official URL itself was not downloaded from this environment. This is an environment DNS limitation, not a build failure.

## Archive extraction verification

A preliminary ZIP was extracted into a clean directory with no Gradle caches or module build directories. From that extracted source tree:

```text
frameworkCheck: BUILD SUCCESSFUL
publishToMavenLocal: BUILD SUCCESSFUL
standalone consumer test: BUILD SUCCESSFUL
```

This confirms that the deliverable is not dependent on build outputs left in the working directory. Details are recorded in `archive-extraction-check.txt`.

## Distribution verification

All generated JARs passed SHA-256 verification. See:

- `distribution-SHA256SUMS.txt`
- `distribution-check.txt`

## Checks not performed against live external systems

The following require organization-specific infrastructure or credentials and were not executed live:

- launching a real Chrome/Firefox/Edge/Safari session
- Selenium Grid
- BrowserStack
- Sauce Labs
- Jira
- PostgreSQL or Microsoft SQL Server
- MongoDB
- Elasticsearch

Their modules were dependency-resolved, compiled and packaged. Provider-independent logic was unit/smoke tested where practical.

## Explicitly limited legacy features

- Internet Explorer, PhantomJS and HtmlUnit are not supported.
- Perfecto intentionally throws an explanatory exception until a dedicated W3C/Appium provider is supplied.
- Jira feature generation tied to the original project-specific issue schema remains unsupported.
- `cf-har-utils` captures Chromium performance-log JSON; it does not claim to generate a fully normalized HAR archive.

## Javadoc status

Javadoc JAR generation succeeded for all modules. The legacy codebase still produces documentation warnings for public members without comments; these warnings do not fail the build and are a documentation-quality backlog item rather than a compilation defect.
