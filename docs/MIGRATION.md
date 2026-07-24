# Migration notes: 2.x to 3.x

Version 3 is a breaking modernization. It preserves the framework's execution model where practical but does not attempt binary compatibility with JARs compiled against the old Cucumber 1/Selenium 3 APIs.

## Build system

- Gradle 4.4 was replaced with Gradle 8.14.5.
- `compile`, `uploadArchives` and the legacy Maven plugin were replaced by `api`, `implementation` and `maven-publish`.
- Every module produces the main JAR, `-sources.jar` and `-javadoc.jar`.
- Repository credentials are read from protected Gradle properties or environment variables.

## Java

- Production and test sources compile with `--release 17`.
- The build may run on JDK 17 or a newer JDK.

## Cucumber and TestNG

- `cucumber.api.*` and `info.cukes.*` were replaced by `io.cucumber.*`.
- The TestNG runner now executes Cucumber scenarios through `provideScenarios()` and `runScenario()`.
- Scenario/browser combinations are exposed as individual TestNG invocations.
- `strict = true` was removed because it is not part of modern `@CucumberOptions`.
- `DataTable.raw()` was replaced by `DataTable.cells()`/typed conversion APIs.

## Thread safety

- The process-wide list of thread contexts was replaced with `ThreadLocal<ThreadContext>`.
- WebDriver managers and WireMock servers are thread-confined.
- Hooks clear scenario state in `finally` blocks.

## Selenium

- Selenium 3 was replaced with Selenium 4.
- waits use `Duration`.
- browser-specific `Options` and W3C capabilities replace legacy `DesiredCapabilities` behavior.
- custom capabilities must be vendor namespaced, such as `sauce:options` or `bstack:options`.
- Selenium Manager handles drivers by default.
- PhantomJS, HtmlUnit and Internet Explorer execution were removed.
- legacy BrowserStack `os` and `os_version` values are translated into `bstack:options`.

## API

- REST Assured was upgraded to 5.x.
- unsupported methods and parameter types now fail immediately with an explanatory exception.
- Swagger validation was replaced by the current Atlassian OpenAPI validation filter wrapper.
- request state remains scenario scoped rather than global.

## Reporting

- the obsolete custom Extent formatter depended on removed Gherkin internals and was removed.
- the framework now logs and attaches artifacts through the public Cucumber `Scenario` API.
- text summaries are generated from Cucumber JSON.
- consumer projects may add Allure or Extent adapters explicitly.

## Configuration

- Commons Configuration 1 types were removed from the public API.
- `FrameworkProperties` provides the typed accessors required by the framework using JDK `Properties` internally.

## Database integrations

- JDBC connections are created and closed per operation.
- MongoDB uses `MongoClients` from the current synchronous driver.
- the removed Elasticsearch high-level REST client was replaced by a Java `HttpClient` based REST helper so the framework is not hard-bound to an obsolete Elasticsearch client generation.

## Jira, HAR and provider-specific limitations

- Jira REST calls use Java `HttpClient`; missing Jira tags are handled safely.
- project-specific Jira feature generation remains unsupported because the original implementation depended on a proprietary issue schema.
- `cf-har-utils` captures Chromium performance logs. It is diagnostic network JSON, not a fully normalized W3C HAR file.
- Perfecto support requires a separate provider implementation with account-specific W3C/Appium capabilities.

## Source recovery

The original framework directory already contained Java source for the framework modules. The available 2.1.0 common and Selenium JARs were inspected against those source trees; no decompilation was needed.

Three obsolete classes were deliberately removed rather than reconstructed:

- legacy Extent formatter
- PhantomJS driver manager
- HtmlUnit driver manager

`FrameworkProperties` was added as a modern replacement for the old third-party configuration type exposed by the framework.
