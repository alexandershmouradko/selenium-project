# <a name="home">Accenture Open Source Test Automation Framework</a>

***An accelerator and test harness for building automated tests of browser applications and web-services.***

***Supports BDD based testing using a robust set of open source tools and libraries.***

***Reduces the amount of custom code required to test web-services and browser applications.***

***Includes parallel test execution, multi-browser execution, in-built reporting (including screenshot capture), logging, and many other helper utilities and methods.***



<br>

![](documentation\readme\Tooling.png)

<br>

This readme / user guide provides detailed instructions on:

- how to get started with the framework
- how to create a test project
- how to write automated API or Browser tests
- how to execute the tests from IDE, Command Line, Jenkins

A demo project with example tests and a template starter project are also available to clone (links included in this guide).

<br>

### <a name="contents">Table of Contents</a>

- [1. Framework Overview](#framework-overview)
- [2. Getting Started](#getting-started)
- [3. Project Structure](#project-structure)
- [4. Project Configuration](#project-configuration)
- [5. Test Suites](#test-suites)
- [6. Runner Classes](#runner-classes)
- [7. Writing API Tests](#writing-api-tests)
- [8. Writing Browser Tests](#writing-browser-tests)
- [9. Adding Hooks](#hooks)
- [10. Running Tests](#running-tests)
- [11. Runtime Parameters](#runtime-parameters)
- [12. Output Reporting](#output-reporting)
	- [12.1 HTML Output Report (ExtentReport)](#html-report)
	- [12.2 Flat File Text Report](#html-report-flatFile)
	- [12.3 Email Report (Jenkins Pipeline)](#html-report-email)
- [13. Parallel Test Execution](#parallel-test-execution)
- [14. Multi Browser Tests](#multi-browser-tests)
- [15. Parallel Tests and Multi Browser Combined](#parallel-tests-and-multi-browser-combined)
- [16. Jenkins Integration](#jenkins-integration)
- [17. Database Helper](#database-helper)
- [18. Mocking Helper](#mocking-helper)
- [19. JIRA Integration](#jira-integration)
- [20. Framework Enhancement and Collaboration](#framework-enhancement-and-collaboration)
- [21. Template and Demo Projects](#template-and-demo-projects)

<br><br>

### <a name="framework-overview">1. Framework Overview</a>

The framework is written in Java and is broken down into a number of modules.   

***cf_common_utils***  starts up, controls and closes down the test execution. Hooks and listeners provide automatic reporting with logging of test results to a html output report (including aggregated summary and detailed views).   TestNG suites and a TestNG data provider are used to support multi-threaded parallel execution of tests.   

***cf_selenium_utils***  uses a driver factory and configuration files to automatically manage the instantiation of the Selenium WebDriver across browser types and to support parallel multi-browser testing.  Tests can be easily switched (via runtime parameters) to execute locally or remotely on a selenium grid or cloud based testing platform (e.g. saucelabs or browserstack).  Includes a number of base classes to simplify selenium testing including automatic element/event waits, screenshot capture and other commonly used selenium methods.    

***cf_api_utils*** provides a set of helper classes and methods with a set of predefined cucumber step definitions that wrap around the RestAssured library.  These combine to enable codeless testing of Restful API's (aka cucumber feature files only).

***cf_jira_utils*** provides hooks and helper methods to export runnable Cucumber '.feature' files from any acceptance criteria statements included in JIRA (User Story/Feature/Scenario issue types), and following an execution run to update JIRA with run results for each acceptance criteria and to raise JIRA bugs as needed.

***cf_database_utils*** provides a set of helper methods that wrap around the Apache DBUtils library and MongoDB library to enable tests to connect and run queries either via a JDBC or MongoClient connection.

***cf_mocking_utils*** provides a set of helper methods that wrap around the WireMock library to enable tests to start up mock services/end points (aka stubs) at runtime.

<br>

![](documentation\readme\FrameworkArch.png)

[back to top](#home)

<br><br>

### <a name="getting-started">2. Getting Started</a>

Test projects consume and use the framework via a set of jar files (one per module).  These jar files are version controlled and held in a central Accenture [Nexus repository](http://34.252.181.171/nexus/#view-repositories;cukes-framework~browsestorage).

The jars can be downloaded and imported to a test project manually, however, to take advantage of transitive dependency management it is highly recommended that test projects include the jars as compile dependencies within their build management tool (e.g. gradle or maven).

A template project is available to be cloned from an [Accenture Innersource](https://innersource.accenture.com/projects/ATAC/repos/cukes_template/browse) git repo and provides a typical gradle project setup.

```
git clone ssh://git@innersource.accenture.com/atac/cukes_template.git
or
git clone https://your.enterprise.id@innersource.accenture.com/scm/atac/cukes_template.git
```

Alternatively a new gradle or maven test project can be manually created within an IDE such as Eclipse or IntelliJ to follow the [project structure](#project-structure) outlined below.

Note: test projects must consume the *cf_common_utils* module but the remainder of the modules are optional based on the scope of the test project.  For example if a test project only needed to perform basic selenium tests then it would need to include only the *cf_common_utils* and *cf_selenium_utils* jars in the project dependencies.

[back to top](#home)

<br>

<br>

### <a name="project-structure">3. Project Structure</a>

A test project consists of the following key artefacts:

| Artefact         | Type                           | API Tests | Browser Tests |
| ---------------- | ------------------------------ | --------- | ------------- |
| test suites      | xml file                       | yes       | yes           |
| runners          | java class                     | yes       | yes           |
| features         | text file                      | yes       | yes           |
| step definitions | java class                     | optional  | yes           |
| page objects     | java class or json/excel files | no        | yes           |
| configuration    | properties files               | yes       | yes           |

<br>

![](readme/TestProject.png)

<br>

<br>

The basic folder structure for a test project is shown below:     

```
project/  
|---src/test/java/
    |---runners/  
    |---steps/						--> only required for browser testing 
    |---pageobjects/					--> only required for browser testing
    |---hooks/                   								
|---src/test/resources/  
    |---features/   
    |---testsuites/   
    |---config/  
        |---paths.properties  
        |---environments/  
        |---selenium/  					--> only required for browser testing
            |---stacks/
            |---runtime.properties
        |---jira/					--> only required for JIRA integration
    |---pagestructures/					--> only required for semi-codeless browser testing
    |---apistructures/					--> only required for API testing
    |---testdata/
|---gradle or maven config files (build.gradle/settings.gradle or pom.xml)
```



Note: browser and api tests can be included in the same test project and can be executed alongside each other in the same test run.

<br>

##### <a name = "resource-folder">3.1 src/test/resources folder</a>

| Folder                 | Description                                                  |
| ---------------------- | ------------------------------------------------------------ |
| *features*             | Holds the set of cucumber '.feature' files.  This folder can be further subdivided as needed, the framework searches recursively when locating tests to execute. |
| *testsuites*           | Holds the test suite '.xml' file(s) in standard TestNG format.  These suites define the set of tests to be executed (aka the <runner>.java classes). |
| *config/environments*  | Holds the properties file for each test environment with environment specific parameters including the application end-points. |
| *config/selenium*      | Holds the project specific properties file that will be picked up by the framework when invoking selenium webdriver (e.g. to specify custom driver capabilities / runtime options). |
| config/selenium/stacks | Holds json configuration file(s) specifying one or more operating system / browser combinations that tests should be executed against. |
| config/jira            | Holds json configuration file(s) specifying the url and credentials for the project jira instance, along with the data structure/fields to be updated when creating test failure bugs. |
| pagestructures         | Holds json file(s) when adopting semi-codeless browser testing.  The json files replace the need to write java page objects (and significantly simplify step definitions java code) and encapsulate the page layouts/element locators and selenium actions to be performed in json or excel format. |
| apistructures          | Holds json file(s) supporting codeless api testing.  The json files define the locators for finding elements within a json response body and the validation rule to be applied. |
| testdata               | Holds any input/output data files that may be required for the tests (e.g. input data abstracted out of the cucumber feature into json file) |

Within the *src/test/resources/* folder the structure above is recommended as a repeatable format for test projects, however,  the structure can be altered if needed using the paths.properties file (see [project configuration](#project-configuration))

<br>

##### <a name = "java-folder">3.2 src/test/java folder</a>

| Folder        | Description                                                  |
| ------------- | ------------------------------------------------------------ |
| *runners*     | Holds the runner class(es) used to invoke the cucumber features. Each class extends the framework BaseTest class (which itself defines a core set of cucumber options along with the methods required to invoke and manage the tests).  The test project runner classes simply specify the required '@tags' to link to one or more cucumber features. |
| *steps*       | For browser testing holds the step definition classes that implement the gherkin steps defined in the cucumber features.  Each steps class extends the BaseSteps class of the Selenium Utils modules.  For API testing the step definitions required to invoke and interact with RestAssured have been pre-written as part of the framework. |
| *pageobjects* | For browser based testing holds a set of classes (following the standard page objects pattern) that define the UI interface of the application under test and the methods that will interact with the pages or common objects of the application.  Each page object class extends the BasePO class which incorporates the webdriver/webdriver wait objects and common selenium methods.  Page objects are not required for API testing. |
| *hooks*       | Placeholder for any project-specific setup/teardown actions (if any are required in addition to the framework hooks already provided by the Common Utils module and Selenium Utils module). |

Within the *src/test/java/* folder the structure above is recommended as a repeatable format for test projects, however,  any code package organisation can be used.

For browser testing see also [Page Objects](https://github.com/SeleniumHQ/selenium/wiki/PageObjects) on the Selenium HQ Wiki.  The page object pattern is not mandated for use with this framework but is recommended as a standard best practice.  The Selenium PageFactory pattern for implementing page objects can also be used and the BasePO class includes the necessary instantiation of any PageFactory classes.

<br>

build.gradle  
settings.gradle
gradle.properties

##### <a name="settings-gradle">3.3 Gradle Project</a>

There are 3 config files held at the root of the test project:

- Settings.gradle

- Build.gradle

- Gradle.properties

  

The ***Settings.gradle*** file has a single entry defining the name of the test project

```
rootProject.name = 'MyTestProject'
```



The ***Build.gradle*** file defines the projects dependencies on the framework modules, the repositories to look in for these dependencies and a custom gradle task (named 'cukes') for executing the framework.

All transitive dependencies needed by the open source utilities of the framework will be automatically downloaded. For example the following gradle file will pull down 6 framework modules and all additional transitive jar files. 

```
apply plugin: 'java-library'
apply plugin: 'maven'

dependencies {
compile group: 'com.acn.ndc', name: 'cf_common_utils', version: '0.0.9' 		
compile group: 'com.acn.ndc', name: 'cf_api_utils', version: '0.0.5'			
compile group: 'com.acn.ndc', name: 'cf_database_utils', version: '0.0.5'		
compile group: 'com.acn.ndc', name: 'cf_mocking_utils', version: '0.0.4'		
compile group: 'com.acn.ndc', name: 'cf_selenium_utils', version: '0.0.11'		
compile group: 'com.acn.ndc', name: 'cf_jira_utils', version: '0.0.2'		
}

repositories {
    jcenter()
    maven {
    	credentials {
            username "$nexusUser"
            password "$nexusPassword"
        }
          url "$nexusURL"
    }
}

task cukes (type: Test) {
def suite = "$suiteDIR" + System.getProperty("cukes.testsuite")+'.xml'
systemProperty "cukes.env", System.getProperty("cukes.env")
systemProperty "cukes.browsercombo", System.getProperty("cukes.browsercombo")
systemProperty "cukes.tags", System.getProperty("cukes.tags")

ignoreFailures = true
useTestNG() {
	useDefaultListeners = true
	suites suite
	}
outputs.upToDateWhen { false }
}
```

The gradle.properties property file defines the url and login credentials for the Nexus repository that holds the framework and the folder that holds the projects test suites.

```
nexusUser=username
nexusPassword=password
nexusURL=http://34.252.181.171/nexus/content/repositories/cukes-framework
suiteDIR=src/test/resources/testsuites/
```



##### <a name="settings-gradle">3.4 Maven Project</a> 

The ***pom.xml*** file defines the projects dependencies on the framework modules and the repositories to look in for these dependencies.

All transitive dependencies needed by the open source utilities of the framework will be automatically downloaded. For example the following *pom.xml* file will pull down 6 framework modules and all additional transitive jar files. 

```
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>com.acn.ndc</groupId>
	<artifactId>healthconference</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>healthconference</name>
	<description>health conference test project</description>
<properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
  </properties>
	<dependencies>
		<dependency>
			<groupId>com.acn.ndc</groupId>
			<artifactId>cf_common_utils</artifactId>
			<version>0.0.14</version>
		</dependency>
		<dependency>
			<groupId>com.acn.ndc</groupId>
			<artifactId>cf_api_utils</artifactId>
			<version>0.0.10</version>
		</dependency>
		<dependency>
			<groupId>com.acn.ndc</groupId>
			<artifactId>cf_database_utils</artifactId>
			<version>0.0.9</version>
		</dependency>
		<dependency>
			<groupId>com.acn.ndc</groupId>
			<artifactId>cf_jira_utils</artifactId>
			<version>0.0.6</version>
		</dependency>
		<dependency>
			<groupId>com.acn.ndc</groupId>
			<artifactId>cf_mocking_utils</artifactId>
			<version>0.0.8</version>
		</dependency>
		<dependency>
			<groupId>com.acn.ndc</groupId>
			<artifactId>cf_selenium_utils</artifactId>
			<version>0.0.18</version>
		</dependency>
	</dependencies>

	<repositories>
		<repository>
			<id>cukes-framework</id>
			<name>cukes automation framework</name>
			<url>http://34.252.181.171/nexus/content/repositories/cukes-framework</url>
		</repository>
	</repositories>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<version>2.19.1</version>
				<configuration>
					<suiteXmlFiles>
						<suiteXmlFile>src/test/resources/testsuites/browsertests.xml</suiteXmlFile>
					</suiteXmlFiles>
				</configuration>
			</plugin>
		</plugins>
	</build>

</project>
```

The username/password credentials for the nexus repository should be included in the settings.xml file of the maven installation as follows:

```
<servers>
    <server>
      <id>cukes-framework</id>
      <username>username</username>
      <password>password</password>
    </server>
</servers>
```



##### <a name="settings-gradle">3.5 Fat Jar</a> 

Optionally a project can also generate a fat jar (or uber Jar) containing the test project, the framework and all transitive dependencies (e.g. if projects wants to precompile their test projects, deploy and then execute separately). The snippet below shows the additional lines that would be needed in the *build.gradle* to support this.

```
apply plugin: 'java'
apply plugin: 'application'

task fatJar(type: Jar) {
    manifest {
        attributes 'Main-Class': 'app.TestApplication'
    }
    baseName = 'yourJarName'
	from { configurations.testRuntime.collect { it.isDirectory() ? it : zipTree(it) }}
	from sourceSets.test.output
}

mainClassName = 'TestApplication'
```

<br>

<br>

[back to top](#home)

<br><br>

### <a name="project-configuration">4. Project Configuration</a>

##### <a name="paths-properties">4.1 Paths Properties File</a>

The *config/paths/* folder holds the *paths.properties* file which is a placeholder file to allow projects to set the paths that are used by the framework.   

The default values are listed below but can be amended as needed if a test project wants to use a different structure.

```
environmentsPath=/src/test/resources/config/environments/
seleniumRuntimePath=/src/test/resources/config/selenium/
seleniumStackPath=/src/test/resources/config/selenium/stacks/
jiraPath=/src/test/resources/config/jira/
mockServicePath=/src/test/resources/mocks/
apiStructurePath=/src/test/resources/apistructures/
pageObjectsPath=/src/test/resources/pagestructures/
testDataPath=/src/test/resources/testdata/
reportPath=./RunReports/
textReportPath=./RunReports/report.txt
```

- *environmentsPath* specifies the location of the [environment properties](#environment-properties) files that define the application end points for each test environment
- *seleniumRuntimePath* specifies the location of the [selenium runtime properties](#selenium-runtime-properties) files that specify any custom 'capabilities' or 'options' that a projects wants to be applied when using Selenium WebDriver
- *seleniumStackPath* specifies the location of the [Browser Combo json](#webdriver-management) files defining the set of operating system and browser type/versions that tests will be executed against
- *jiraPath* specifies the location of the [jira configuration json](#jira-integration) files defining the structure and field values for jira bugs (only needed if test project wants to raise JIRA bugs for test failures)
- *mockServicePath* specifies the location of the [wiremock json](#mocking-helper) files that define the stubs to be setup for testing (only needed if stubs are being used)
- *apiStructurePath* specifies the location of the [api json](#api-test-configuration) files that define how an api response can be read and validated (only needed for api testing)
- *pageObjectsPath* specifies the location of the [page structure json](#page-objects-codeless) files that define the application page layouts and selenium actions to be performed (only needed for browser testing in support of codeless page objects)
- *testDataPath* specifies the base location of test data json/excel files that will be used during testing (used by the JSON and Excel Helper classes)
- *reportPath* specifies the location in which the framework should write the output run report
- *textReportPath* is optional and specifies the location to save the simple text results. Can be used for email body.



##### <a name="environment-properties">4.2 Environment Properties File</a>

The *src/test/resources/config/environment/* folder holds 1 or more environment properties files.  These files contain environment specific information such as browser and api application end points and database connections as a set of key=value pairs.

The framework reads the [runtime parameter](#runtime-parameters) *cukes.env* which specifies the environment (aka the name of the property file) to be used for the execution and loads this into the ThreadContext object. 

To avoid hardcoding of URLs in Cucumber features the framework includes a pre-defined Gherkin statement and step definition that read the application end-points from the ThreadContext and can be used for both api and browser testing.

The gherkin statement is: Given an application "*name of app*"

For example if the runtime parameter was set as `-Dcukes.env=devtest` then the endpoints for the application would be loaded from the *devtest.properties* file into the ThreadContext object.  

If the devtest.properties file had the following 3 end-point entries:

```
HealthConference://localhost:8080/automationacademy.designsimplicity.net
LocationAPI=//localhost:8081/getlocation
JourneyAPI=//localhost:8090/getJourney
```

Then a statement in a Cucumber scenario:  

*Given an application "LocationAPI"*  would use the endpoint *//localhost:8081/getlocation* for that test

If a second E2E.properties file had 3 end-point entries:

```
HealthConference://10.100.200.300:8080/automationacademy.designsimplicity.net
LocationAPI=//10.100.200.300:8081/getlocation
JourneyAPI=//10.100.200.300:8090/getJourney
```

Then re-running the same feature tests with the runtime parameter set as `-Dcukes.env=E2E` would use the endpoint *//10.100.200.300:8081/getlocation* for that test.

This allows the cucumber features to be written with generic application end point references and for these tests to be executed against multiple environments simply through configuration of the environment property files and setting of the `cukes.env` runtime property.

[back to top](#home)

<br><br>

### <a name="test-suites">5. Test Suites</a>

For both API and Browser testing a Test project will create one or more suites which are written as xml files and stored in the *src/test/resources/testsuites* folder.

Each suite defines a *test* or set of *tests* which each include one or more [runner](#runners) classes for execution.  The runner classes in turn cross reference one or more feature files.

The following example test suite called "suite1" defines 2 tests called "AccountManagement" and "DirectDebits" and these tests each include a set of runner classes.

```
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="suite1">
    <test name="AccountManagement">
        <classes>
         	<class name="runners.CreateAccount"/>
         	<class name="runners.UpdateAccount"/>
        </classes>
    </test>
    <test name="DirectDebits">
        <classes>
         	<class name="runners.ViewDD"/>
         	<class name="runners.AddDD"/>
         	<class name="runners.DeleteDD"/>
        </classes>
    </test>
</suite>
```

When the framework execution is triggered with a runtime parameter of `-Dcukes.testsuite=suite1` then all of the features cross referenced by these runner classes will be tested.

A test suite can combine both API and Browser tests together.

See also [Parallel Test Execution](#parallel-test-execution) for further details on how to setup parallel test execution using test suites.

[back to top](#home)

<br>

<br>

### <a name="runner-classes">6. Runner Classes</a>

For both API and Browser testing a Test project will create one or more simple runner classes that will be used to trigger the cucumber tests.  The runner classes extend the ***BaseTest*** class of the framework.  

The test project runner classes are coded as follows:

```
package runners;
import cucumber.api.CucumberOptions;
import uk.ndc.csa.utilities.common.BaseTest;

@CucumberOptions(
		features = {"classpath:features/"},
		glue = {"steps", "hooks"},
		tags = {"@yourtags", "~@ignore"})

public class YourRunner extends BaseTest{}
```

<br>

Within the ***CucumberOptions***:

- *features* specifies the folder within which Cucumber will search for features when running a test.  By default the folder will be searched recursively so any sub-folder structure can be applied by a test project
- *glue* specifies the package within which Cucumber will search for step definition classes when running a test.  By default the folder will be searched recursively so any sub-folder structure can be applied by a test project
- *tags* specifies the filtering/selection of tests that should be executed at runtime based on the annotations defined in the feature files



The test project runner class(es) inherit from the ***BaseTest*** class the following functionality:

- set cucumber options to pick up the inbuilt framework hooks, common step definitions 

- set cucumber options to use the framework report listener

- TestNG @before/@after hooks to start up and close down the reporting

- Cucumber @before/@after hooks to setup the ThreadContext helper object with details of the test and perform a fail-safe catch for assertion errors

- TestNG @Test method to invoke TestNG / Cucumber for api and browser tests, utilising a *Data Provider* method, to trigger multi-threaded execution for multi-browser testing

  

A simple test project may require only a single runner class, however, typically multiple runner classes are created using the *tags* option to specify a subset of one or more features that each runner will execute.  

In particular using multiple runner classes enables more granular control over the selection of which tests to execute and the ordering of tests via [test suites](#test-suites) (including parallel test execution). 



Note: when running a mixture of api and browser tests it is also possible to enable/disable the start up of the browser by setting the cukes.selenium runtime parameter directly within each runner class as follows:

```
package runners;
import cucumber.api.CucumberOptions;
import uk.ndc.csa.utilities.common.BaseTest;

@CucumberOptions(
		features = {"classpath:features/"},
		glue = {"steps", "hooks"},
		tags = {"@yourtags", "~@ignore"})

public class YourRunner extends BaseTest{
    @BeforeClass
		public void setupTest(){
			System.setProperty("cukes.selenium", "true");
	}
}
```

[back to top](#home)

<br>

<br>

### <a name = "writing-api-tests">7. Writing API Tests (features and steps)</a>

A very simple example feature that calls the Google Books public API and checks the response is shown below:

```
Feature: Google Maps Rest API

@googlemaps
Scenario: calculations for london to edinburgh
Given a rest api "GoogleMaps"
Given query parameters
| origins		| London, UK	|
| destinations		| Edinburgh, UK	|
When the system requests GET "/maps/api/distancematrix/json"
Then the response code is 200
And the response body contains
| distance | 666 km			|
| duration | 7 hours 21 mins		|
```

This leverages the frameworks built-in Cucumber DSL (pre-written Gherkin statements and Java Step Definitions) that wraps around the RestAssured library.  This is available to any test project that includes the *cf_api_utils* dependency.  

A test project can trigger GET / POST / PUT / DELETE / PATCH calls with detailed validation of the responses simply by re-using the built-in Gherkin statements within the test project features.  No further coding is needed beyond the simple runner class described in the previous section.

Re-using the frameworks common steps can significantly reduce the amount of custom code (and repetitive code) that a project needs to create when testing rest Web-Services.

##### <a name="api-common-steps">7.1 API Common Steps</a>

The following list gives the common Gherkin statements that are available within the framework and their associated step definition methods.

*To specify the base uri/path/port for the API using an end point defined in the test project configuration*

```
Given a rest api "foo"

@Given("^a rest api \"(.*)\"$")
public void public void setAPI (String api)
```

where "foo" references an end point defined in the test project environment properties file (e.g. foo=//localhost:8081/googleapis.com/books/)"

<br>*To define a base uri and port value for the API directly in the feature:*

```
Given a base uri "foo"
Given a port bar

@Given("^a base uri \"(.*)\"$")
public void setBaseURI(String uri)

@Given("^a port (\\d+)$")
public void setPort(int port)
```

where foo = the end point url, and bar is a port number (e.g. 8080)

<br>*To define a list of header values for the API call (accepts list of one or more header values as data table)*

```
Given a header
|key	|value	|

@Given("^a header$")
public void setHeader(Map<String, String> map)
```

<br>

*To define a list of parameters for the API call (accepts list of one or more parameter values as data table)*

```
Given form parameters
|key	|value	|

Given query parameters
|key	|value	|

Given path parameters
|key	|value	|

@Given("^(form parameters|query parameters|path parameters|parameters)$")
public void withParams(String type, Map<String, String> map)
```

<br>*To specify the json data for the request body of the API from a test data file*

```
Given a request body "<<foo.bar>>"

@Given("^a request body \"(.*)\"$")
public void requestBody(String data)
```

Where foo=the name of a json file, bar=a json object within the file, for example:

```
{
	"bar":{
		json body for test case 1 goes here
	},
	"anotherCase":{
		json body for test case 2 goes here		
	}	
}
```

<br>*To specify the json data for the request body of the API as a json string*

```
Given a request body "foo"

@Given("^a request body \"(.*)\"$")
public void requestBody(String data)
```

Where "foo" is a json string such as "{"field1" : "some value", "field2" : "some value"}"

<br>*To define a base data set for an API body which is common to multiple tests*

```
Given base input data "foo.bar"

@Given("^base input data \"([^\"]*)\"$")
public void setBaseInputData(String arg1)
```

Where foo=the name of a json file, bar=a json object within the file

<br>*To trigger the API call (to end point defined by the base path/port + the path defined in this step*

```
When the system requests GET "bar"
When the system requests PUT "bar"
When the system requests POST "bar"
When the system requests PATCH "bar"
When the system requests DELETE "bar"

@When("^the system requests (GET|PUT|POST|PATCH|DELETE) \"(.*)\"$")
public void apiGetRequest(String apiMethod, String path)
```

<br>*To validate the response code from the API call*

```
Then the response code is 200

@Then("^the response code is (\\d+)$")
public void verify_status_code(int code)
```

<br>*To validate the response time from the API call*

```
Then the response time is less than 1000 milliseconds

@Then("^the response time is less than (\\d+) milliseconds$")
public void verifyResponseTime(long duration)
```

<br>*To validate the response body matches an expected schema format*

```
Then the response matches the json schema "foo"
Then the response matches the xml schema "foo"

@And("^the response matches the (json|xml) schema \"(.*)\"$")
public void matchJSONSchema(String type, String path)
```

<br>*To validate the response header contains the expected values (accepts list of one or more header values as data table)*

```
Then the response header contains
|key	|value	|

@Then("^the response header contains$")
public void verifyHeader(Map<String, String> map)
```

<br>*To validate that the response body is empty*

```
Then the response body is empty

@And("^the response body is empty$")
public void responseBodyEmpty()
```

<br>*To validate the response body contents against a pre-defined expected results json* (see below for further details)

```
Then the response body contains
|<<foo.bar>>	|

@And("^the response body contains$")
public void responseBodyValid(DataTable table)
```

Where foo=the name of a json file, bar=a json object within the file

<br>To validate the response body for a specific set of fields (see below for further details)

```
Then the response body contains
|field		|value	|

Then the response body contains
|element	|matcher	|value		|type	|

@And("^the response body contains$")
public void responseBodyValid(DataTable table)
```

<br>*To trace out the request and response json strings*

```
And trace out request response

@And("^trace out request response$")
public void traceOut()
```

<br>

###### Setting Base Data for JSON Body

The step *Given base input data* enables a root json object to be applied as the base data for all subsequent tests.

When used alongside the step *Given a request body* it enables multiple tests to be defined that apply delta's or variations to the base data without the need to repeat/duplicate the full data set.  The data from the *Given a request body* step is overlaid onto the base data replacing any common fields whilst retaining the rest of the base data.

For example with a base data file called BaseData.json:

```
{
	"sunnyDay":{
		"field1":"abc",
		"field2":"def",
		"field3":"ghi"
		"field4":123
	}
}
```

And the following steps in a cucumber scenario:

```
Given base input data "BaseData.sunnyDay"
And a request body "{"field1" : "zzz", "field4": 777}"
```

Would result in the following json body for the test:

```
{
	"sunnyDay":{
		"field1":"zzz",
		"field2":"def",
		"field3":"ghi"
		"field4":777
	}
}
```

This approach can be used in conjunction with Cucumber Example tables to create multiple tests where only some part of the json body varies in value.

note: the json data overlay uses recursive merging and therefore handles nested objects and arrays.

###### <br>Validating the Response Body

The step *Then the response body contains* enables validation of the API response body.  The step accepts a datatable and based on the contents of this table will either compare the entire response body against a predefined expected results JSON object, or will perform a field by field comparison based on a supplied field + expected results list.

To validate against a JSON object a single row, single column table would be defined in the feature:

```
Then the response body contains
|<<foo.bar>>	|
```

Where foo=the name of a json file and bar=a json object within the file, for example:

```
{
	"bar":{
		json body for test case 1 goes here
	},
	"anotherCase":{
		json body for test case 2 goes here		
	}	
}
```

note: the << >> prefix/suffix around the filename.object is simply to make it clear in the feature that the expected results are being taken from an external json file.

<br>To validate the response body for a specific set of fields then the datatable is supplied with either 2 or 4 columns.  Each row in the table details a field to be validated and the expected results.

When using a 4 column table all the values needed to locate each field and perform the validation are defined directly in the feature including the [json path query](#json-path-query) needed to find the element, the [hamcrest matcher](#hamcrest-matcher) to be applied, the expected value and the [data type](#data-types) (such as string).

For example validating fields returned by the GoogleBooks public API:

```
Then the response body contains
|element				|matcher	|value		|type	|
|items.volumeInfo.title			|equalTo	|Steve Jobs	|str	|
|items.volumeInfo.pageCount		|hasItem	|630		|int	|
|items.volumeInfo.averageRating		|hasItem	|4.0		|num	|
```



When using a 2 column table the feature defines the list of functional fields and expected values and is supported by a json file that defines for each field the [json path query](#json-path-query) needed to find the element, the [hamcrest matcher](#hamcrest-matcher) to be applied and the [data type](#data-types).

Using the same example as above:

```
Then the response body contains
|title			|Steve Jobs	|
|pageCount		|630		|
|rating			|4.0		|
```

The supporting json would be structured as:

```
{
	"title":{
		"element":"items.volumeInfo.title",
		"matcher":"equalTo",
		"type":"str"
	},
	"pageCount":{
		"element":"items.volumeInfo.pageCount",
		"matcher":"hasItem",
		"type":"int"
	},
	"rating":{
		"element":"items.volumeInfo.averageRating",
		"matcher":"hasItem",
		"type":"num"
	}
}
```

The 2 column approach is recommended since this simplifies the cucumber feature.

<br>

###### <a name="json-path-query">7.2 Json Path for Locating Elements in the Response Body</a>

RestAssured and therefore this framework use the JsonPath ([GPath](http://groovy-lang.org/processing-xml.html#_gpath)) syntax for locating objects and elements within the response body.

Nested elements within the response are identified by the `.` symbol.  

A json path of `a.b.c` will locate element c nested within object b nested within object a. 

A json path of `a.b[1].c` will locate an element c nested within the second occurrence of array b within object a

RestAssured also accepts groovy queries and filters as part of the json path including:

- `find` – finds the first item matching a closure predicate
- `findAll` – finds all the items matching a closure predicate
- `collect` – collect the return value of calling a closure on each item in a collection
- `sum` – sum all the items in the collection
- `max`/`min` – returns the max/min values of the collection
- `size` - returns the number of elements of the collection



For example the json path:

 `store.book.findAll { it.price < 10 }.title` 

would return an array/collection with the 1st and 3rd item titles from the below json i.e. [Sayings of the Century, Moby Dick]

```
{  
   "store":{  
      "book":[  
         {  
            "author":"Nigel Rees",
            "category":"reference",
            "price":8.95,
            "title":"Sayings of the Century"
         },
         {  
            "author":"Evelyn Waugh",
            "category":"fiction",
            "price":12.99,
            "title":"Sword of Honour"
         },
         {  
            "author":"Herman Melville",
            "category":"fiction",
            "isbn":"0-553-21311-3",
            "price":8.99,
            "title":"Moby Dick"
         }
      ]
   }
}
```

<br>

###### <a name = "hamcrest-matcher">7.3 Hamcrest Matchers</a>

RestAssured (and therefore this framework) use Hamcrest matchers when validating the field values held within the json response body from an API call.

The available matchers in this framework for single object validation are:

| Matcher        | Description                                                  |
| -------------- | ------------------------------------------------------------ |
| equalTo        | checks that the value of the object in the response matches the expected value |
| isNull         | checks that an object in the response is null                |
| isEmpty        | checks that a string field in the response is empty          |
| startsWith     | checks that a string field in the response starts with the expected text |
| endsWith       | checks that a string field in the response ends with the expected text |
| containsString | checks that a string field in the response contains the expected text |

The available matchers in this framework for array validation are:

| Matcher             | Description                                                  |
| ------------------- | ------------------------------------------------------------ |
| hasItem             | checks that an array object includes at least the expected value |
| hasItems            | checks that an array object includes at least the list of expected values ** |
| contains            | checks that an array object contains only the list of expected values ** and in the same order |
| containsAnyOrder    | checks that an array object contains only the list of expected values ** but in any order |
| hasSize             | checks that an array object has at least one entry           |
| containsStringArray | checks that a string array contains the expected text        |

** A list of expected values are specified as a comma separated list in square brackets i.e. in the format `[a,b,c]`.   

Some examples:

```
|matcher		|expected value	|
|equalTo		|a		|		
|hasItem		|a		|		
|hasItems		|[a,b]		|    	 
|contains		|[a,b,c]	|		 
|containsAnyOrder	|[a,b,c]	|		 
```

##### <a name = "data-types">7.4 Data Types</a>

When defining elements in the JSON response for validation it is necessary to specify the data type as one of either `str` for String values, `int` for integer values or `num` for floats.



##### <a name="json-path-hamcrest">7.5 Combining JSON Path and Hamcrest Matchers for Validation</a>

Given the API response body below then the following entry in a cucumber feature file shows some example validations. 

```
Then the response body contains
|element						|matcher	|value			|type|	
|store.book[0].author					|equalTo	|Nigel Rees		|str |	
|store.book.findAll {it.category=='fiction'}.sum()	|equalTo	|21.97			|num |
|store.book.title					|hasItems	|[Life of Pi,Moby Dick]	|str |

```



```
{  
   "store":{  
      "book":[  
         {  
            "author":"Nigel Rees",
            "category":"reference",
            "price":8.95,
            "title":"Sayings of the Century"
         },
         {  
            "author":"Yann Martel",
            "category":"fiction",
            "price":12.99,
            "title":"Life of Pi"
         },
         {  
            "author":"Herman Melville",
            "category":"fiction",
            "isbn":"0-553-21311-3",
            "price":8.99,
            "title":"Moby Dick"
         }
      ]
   }
}
```

<br>



##### <a name="example-feature">7.6 Example Cucumber Features</a>

###### Example 1 - GET Request

This example shows 2 working scenarios with GET requests to the public GoogleMaps API and validates the response. 

```
Feature: Google Maps Rest API

@google @maps
Scenario: calculations for london to edinburgh
Given a rest api "GoogleMaps"
Given query parameters
| origins		| London, UK		|
| destinations		| Edinburgh, UK		|
When the system requests GET "/maps/api/distancematrix/json"
Then the response code is 200
And the response body contains
| distance | 666 km		|
| duration | 7 hours 21 mins	|


@google @maps
Scenario Outline: calculations for <start point> - <end point> - <travel type>
Given a rest api "GoogleMaps"
Given query parameters
| origins		| <start point> |
| destinations		| <end point>	|
| mode			| <travel type> |
When the system requests GET "/maps/api/distancematrix/json"
Then the response code is 200
And the response body contains
| distance 		| <distance>	|
| duration 		| <duration> 	|

Examples: 
| start point | end point	| travel type	| distance	| duration			|
| London, UK  | Edinburgh, UK	| driving		| 666 km	| 7 hours 21 mins	|
| London, UK  | Edinburgh, UK	| walking		| 607 km	| 5 days 4 hours	|
| London, UK  | Edinburgh, UK	| bicycling		| 713 km	| 1 day 14 hours	|
| Bath, UK    | York, UK	| driving		| 377 km	| 4 hours 10 mins	|
| Bath, UK    | Paris, FR	| driving		| 641 km	| 7 hours 21 mins	|
```

This is supported by the following entry in the environment property file:

```
GoogleMaps=https://maps.googleapis.com
```

And by the following json file which defines how to access the fields distance and duration in the response:

```
{
	"status":{
		"element":"status",
		"matcher":"equalTo",
		"type":"str"
	},
	"distance":{
		"element":"rows.elements.distance.flatten().text",
		"matcher":"contains",
		"type":"str"
	},
	"duration":{
		"element":"rows.elements.duration.flatten().text",
		"matcher":"contains",
		"type":"str"
	}
```



###### Example 2 - POST Request to Swagger demo pet store site

This example shows multiple working POST and GET requests to the public demo API Pet Store.  The POST request includes setting a default json body for re-use across 3 tests with replacement of the id and name elements.

```
Feature: Pet Store Rest API

@petstore
Scenario Outline: Create Pet Information
Given a rest api "PetStore"
Given a header
|Content-Type	| application/json		|
And base input data "PetStoreTestData.default"
And a request body "{"id":<petID>,"name":"<petName>"}"
When the system requests POST "/v2/pet/"
Then the response code is 200

Examples:
|petID	|petName	|
|1	|Spot		|
|2	|Whiskers	|
|3	|Rover		|

@petstore
Scenario Outline: Get Pet Information
Given a rest api "PetStore"
Given a header
|Content-Type	| application/json		|
And path parameters
|petID	|<id>	|
When the system requests GET "/v2/pet/{petID}"
Then the response code is 200
And the response body contains
|element	|matcher	|value		|type	|
|name		|equalTo	|<petName>	|str	|

Examples:
|id		|petName	|
|1		|Spot		|
|2		|Whiskers	|
|3		|Rover		|
```

In support of this example the PetStoreTestData.json file had the following content:

```
{
	"default": {
		"id": 0,
		"category": {
			"id": 0,
			"name": "string"
		},
		"name": "my new pet",
		"status": "available"
	}
}
```





##### <a name="api-custom-steps">7.7 Custom Steps</a>

Additional custom steps can also be created by a test project to work alongside or extend these framework steps.  

To support this the Framework includes a context object which gets populated with all the api request / response data from the framework steps as the test execution progresses.  

The simplest way for any custom step classes to access this context object and its data is to use the built-in Pico dependency injection via a constructor method in the custom steps class.  This is shown below:

```
public class YourProjectSteps {
	RestContext restContext;
	
	public YourProjectSteps(RestContext restContext){
		this.restContext = restContext;
	}
```

All of the request / response data (and the associated methods to access this) will then be available in the *restContext* instance variable of the custom steps class.

##### <a name="api-test-configuration">7.8 API Test Configuration</a>

As described above, when using the *"Then the response body contains"* built-in step from the framework DSL there is the option to simplify the feature by defining the way the step reads the API response outside of the feature and in a json configuration file.  

For each element of the API response to be validated the configuration file defines the field name, the JsonPath query used to locate the element, the hamcrest matcher to be applied, and the data type of the element.

The configuration file name should be *"\<API\>.json"* where *\<API\>* is the same value as used to define the API end point in the environment properties file.  

The file should be held in the test project under *src/test/resources/config/apistructures/*.

##### <a name = "test-assertions">7.9 Test Assertions</a>

The framework uses *soft assertions* in each of the built-in steps when performing the validation of the api response and reports an aggregated result.  For example if 5 fields in the response body were being checked and 2 of these failed then both failures will be included in the output report for that step.

[back to top](#home)

<br>

<br>

### <a name = "writing-browser-tests">8. Writing Browser Tests (features, steps and page objects)</a>

The framework includes a number of re-usable components for simplifying the creation of selenium tests:

- ***BaseSteps*** and ***BasePO*** classes which form the basis for any step definitions and page objects created for the test project and which include a number of commonly used methods
- ***Element*** class which wraps around the Selenium WebElement object and provides in-built waits, retries and other commonly used functions
- ***DriverFactory***, ***DriverManager***, and ***Capabilities*** classes for management of the selenium WebDriver
- ***Screenshot*** class for capturing and saving screenshots
- ***Hooks*** class which handles the start-up / close-down of the driver factory and screenshot / reporting on test failure

These capabilities are available to any test project that includes the cf_selenium_utils dependency.  

##### <a name="features">8.1 Features</a>

The framework includes a pre-written Gherkin statement and Java Step Definition that can be used in any feature to launch the application under test based on a URL defined in the test project environment properties file (e.g. foo=//localhost:8081/myApplication/launch)

```
Given the application "foo"

@Given("^the application \"(.*)\"$")
public void launchApplication(String app){
    driver.manage().window().maximize();
	String url = ThreadContext.getInstance().getEnvironmentProps().getString(app); 
	driver.get(url);	
}
```

This allows the cucumber features to be written with generic application end point references and for these tests to be executed against multiple environments simply through configuration of the environment property files and setting of the `cukes.env` [runtime property](#runtime-parameters).



This built-in step is then supplemented by custom gherkin statements that are defined and implemented in the test project.  A simple example feature that performs an application login might look like:

```
Feature: Login to application

@login
Scenario: login with valid credentials
Given the application "MyPortalApp"
When a user logs in with "foo" "bar"
Then the homepage greeting message is correct
```



All the standard functionality of [Cucumber](https://cucumber.io) is available and whilst a detailed description of cucumber feature file+step definition writing is beyond the scope of this document, the following extract from a simple cucumber feature illustrates a number of key points including:

- use of standard Given, When, Then style acceptance criteria language (Gherkin natural language)
- use of example tables to enable a scenario to be defined once but executed with multiple datasets
- use of @tags which allow filtering of tests to be executed at runtime

```
Feature: Delegate Journey Planner
  As an interested conference delegate
  I want to view journey details to the venue
  So that I can find the best way of getting to the conference

  @journeyplanner @smoketest
  Scenario Outline: Check Journey Calculation
    Given the application "HealthConference" is launched
    When a conference delegate checks their travel details from "<location>" by "<travel type>"
    Then the correct journey information will be calculated as "<distance>" and "<duration>"

    Examples:
      | location       | travel type | distance | duration        |
      | NE1 1AE        | car         | 461 km   | 4 hours 49 mins |
      | YO1 7HH        | car         | 341 km   | 3 hours 43 mins |
      | Romford, Essex | bike        | 21.9 km  | 1 hour 16 mins  |
      | Islington      | bike        | 16.1 km  | 53 mins         |
```

Any number of features and scenarios can be created. The feature file naming and sub-folder structure within *src/test/resources/features* is arbitrary and can be tailored to suit the test projects needs.



##### <a name="steps">8.2 Steps</a>

Java Step Definition classes that are created for the test project should extend the ***BaseSteps*** class and will inherit the instance variables and common methods of this base class.

```
import uk.ndc.csa.utilities.selenium.steps.BaseSteps;
public class MySteps extends BaseSteps{
    .....
}
```

These step classes will have direct access to the ***WebDriver*** object for the currently running thread along with a ***WebDriverWait*** object.  These are defined as instance variables in the base class and are obtained from the driver factory. 

```
public class BaseSteps {
	protected WebDriver driver = DriverFactory.getInstance().getDriver();
	protected WebDriverWait wait = DriverFactory.getInstance().getWait();
	protected BasePO po = new BasePO();
	.....
}
```

The methods of the BaseSteps class are:

```
/** captures a screenshot and adds it to the output report for the currently executing step*/
protected void addScreenshot()

/** performs auto population and validation of elements on a browser page based on JSON configured page objects (aka codeless page objects)*/
public void autoFillCheckJSON(String methodJSON, String methodName, Map<String, String> featureData)

/** performs auto population and validation of elements on a browser page based on Excel configured page objects (aka codeless page objects)*/
public void autoFillCheckExcel(String methodExcel, String methodName, Map<String, String> featureData)

/** performs set of pre-defined selenium operations in support of JSON/Excel configured page objects */
public void performOperation(Element el, String action, String type, String value, String fieldName)

/** uses the findElement method of the BasePO to locates WebElements for JSONExcel configured page objects */
public Element find(String method, String locator)

/** gets the duration used for any WebDriverWait objects created from project config */
private int getWaitDuration()
```



A basic step definition class of the test project should:

- define the step methods that implement the Gherkin statements used in the project	
- invoke page object methods to perform actions on the test application 
- perform test assertions



The framework supports 3 formats for page objects (see later sections for details on page objects):

1. java page objects (coded)
2. json page objects (codeless)
3. excel page objects (codeless)

Using coded page objects will provide more flexibility and is the generally recommended approach, especially for complex applications.  However, for simple applications then either a coded or codeless page object approach can be adopted depending on the test projects preference.

The code required to invoke both styles of page object from a step definition method is shown below for the following example feature:

```
Feature: Login to application

Scenario: login with valid credentials
Given the application "MyPortalApp"
When a user logs in 
|username	|foo	|
|password	|bar	|
Then the homepage greeting message is correct
```

A simple step definition class that invokes coded java page objects may look like:

```
public class LoginSteps extends BaseSteps{
    
    @When("^a user logs in$")
    public void loginToApp(Map<String, String> map){
    	LoginPO login = new LoginPO();
       	login.performLogin(map.get("username"), map.get("password"));
    }
    
   @Then("^the homepage greeting message is correct$")
    public void checkHomepageDisplay(){
    	HomepagePO homepage = new HomepagePO();
        Assert.assertEquals(homepage.getSalutationMsg(), "My Application Homepage");
    }
}
```

In this example the java page object(s) relevant to the test were instantiated within the step definition methods and the member methods invoked to perform some selenium operations.

The example step definition methods below show the code needed to invoke either json or excel codeless page objects:

```
@When("^a user logs in$")
    public void loginToApp(Map<String, String> map){
    	autoFillCheckJSON("LoginMethods", "login", map);
    }
    
    @Then("^the homepage greeting message is correct$")
    public void checkHomepageDisplay(){
        autoFillCheckExcel("LoginMethods", "check salutation", null);
    }
```

The step definition code calls either the autoFillCheckJSON() or autoFillCheckExcel() method of the *BaseSteps* class passing the following parameters:

- name of the file that defines the page object methods
- the method to be invoked
- any test data to be used (in map<String, String> format).

The autoFillCheck() method reads and iterates through the json or excel page objects and performs the required actions, waits and assertions as defined in the page object.

##### <a name="steps">8.3 Sharing Data Across Steps</a>

Test data can be shared across step definition classes and methods in 3 ways:

- Instance variables
- Dependency Injection
- ThreadContext object

###### Instance variables - to share data across methods within the same step class

```
public class MyClass extends BaseSteps{
    private String username;
    
    @When("^a user logs in with \"(.*)\" \"(.*)\"$")
    public void createAccount(String username, String password){
        this.username = username;
       	.....
    }

	.....
}
```

The step class object that is created by Cucumber at runtime persists for the duration of the scenario being executed and therefore any instance variables are available to all methods within the class that are being executed during that scenario. 



###### Dependency Injection - to share data across methods in different step classes

The built-in PICO dependency injection can be used to share any object across multiple step classes.  To do this each step class that needs to access the object includes a constructor method with the shared class as an input parameter.  For example:

```
public class MyClass extends BaseSteps{

SomeSharedObject obj;

MyClass (SomeSharedObject obj){
    this.obj = obj;
}
```

Note: that dependency injection enables data sharing across the step classes layer only and not the page object layer below.



###### ThreadContext object - to share data across methods in different step classes

The framework includes a thread specific Context object can be used to share any data across multiple step classes or be accessed from any test code including page objects.  This object includes a map variable that can be used to store and share values or objects throughout the executing thread.

```
 public class ThreadContext {    
	private Map<String,Object>          testdata = null;    
 	.....
 	public Map<String, Object> testdata(){
		if (testdata ==null )
			testdata = new HashMap<String,Object>();
		return testdata;
	}
 }
```

A test project uses the `testdata()` method of the ThreadContext object to access the map and subsequently get or set entries in the map.  For example:

```
ThreadContext.getInstance().testdata().put("foo", "bar");
.....
ThreadContext.getInstance().testdata().get("foo");
```

Any variable or object type can be added to the testdata map.

For easy access to the testdata, you can use `testdataPut(String key, Object data)` for saving your data, `testdataGet(String key)` for getting your testdata as an object or `testdataToClass(String key, Class<T> type)` which would return your testdata as your data type.
e.g. With a class that contains Person information and anthoer containing a list
```java
class Person {
	String name;
	Date dob;
	String gender;
	String email;
}

class Persons {
	List<Person> persons;
}
```
Using the example scenario
```
Scenario: Submitting multiple people to survay requests
	Given the application "foo"
	When submitting peoples information
		| Name          | Dob        | Gender  | Email                           |
		| Sean Goodmen  | 13/12/1993 | Male    | sean.goodmen@example.com        |
		| Emma White    | 23/09/1991 | Female  | emma.white@example.com          |
		| Harry Hampton | 18/05/1986 | Male    | harry.hampton@example.com       |
	Then the table contains the submitted people
```
We can save the data set and read it in antoher step
```java
	@When("^submitting peoples information$")
	public void submitting_peoples_information(List<Person> persons){
		Persons p = new Persons();
		p.persons = persons;
		ThreadContext.getInstance().testdataPut("personsInformation", p);
	}
	
	@Then("^the table contains the submitted people$")
	public void the_table_contains_the_submitted_people(){
		Persons p = ThreadContext.getInstance().testdataToClass("personsInformation", Persons.class);
	}
```

This process can also be applied to the Dependency Injection to only share information between steps.

##### <a name="page-objects-coded">8.4 Java Page Objects (coded)</a> 

Java Page Objects classes created for the test project should extend the ***BasePO*** class and thereby inherit the instance variables and common methods of the base class.

```
import uk.ndc.csa.utilities.selenium.pageobjects.BasePO; 
public class MyPageObject extends BasePO{
    .....
}
```

When the page object is instantiated in any Cucumber step it will have direct access to the WebDriver object for the currently running thread along with a WebDriverWait object.  These are defined as instance variables in the base class and are obtained from the driver factory.

```java
public class BasePO {
	protected WebDriver driver = DriverFactory.getInstance().getDriver();
	protected WebDriverWait wait = DriverFactory.getInstance().getWait();
	.....
}
```

The methods available to any page object that extends the BasePO are:

```
public BasePO gotoURL(String url)

/** Waits for page to load by performing DOM readiness, JQuery readiness and Angular readiness checks*/
public BasePO waitPageToLoad()

/** Wait for page to load based on document.readyState=complete*/
public void domLoaded()

/** Wait for all JQuery operations on the page to complete */
private void jqueryLoaded()

/** Wait for AngularJs operations on the page to complete */
public void angularLoaded()

/** Returns first element matching the supplied locator using a wait for the element to exist in DOM and scrolls to the element*/
public Element findElement(By by, int...delay)

/** Returns first element matching the supplied locator using a wait for the element to be clickable and scrolls to the element*/
public Element findClickable(By by, int...delay)

/** Returns first element matching the supplied locator using a wait for the specified condition to be met and scrolls to the element*/
public Element findElement(ExpectedCondition<?> exp, int...delay)

/** Returns first nested child element within a parent matching the supplied locators using a wait for the elements to exist in DOM*/
public Element findElement(By by, By sub, int...delay)

/** Returns all element occurrences matching the supplied locator inc. wait for at least one matching element to exist in DOM and scrolls to the first occurence*/
public List<Element> findElements(By by, int...delay)

/** Returns all child elements occurrences within a parent matching the supplied locators using a wait for the elements to exist in DOM and scrolls to the first occurence*/
public List<Element> findElements(By by, By sub, int...delay)

/** Scrolls to element to avoid issues with element location being unclickable */
public Element scroll(Element el)

/** Scrolls to element to avoid issues with element location being unclickable */
public Element scroll(Element el)

/** Checks for element existence */
public boolean exist(By by, int...delay)

/** switches focus of the webdriver to the specified window */
public void switchWindowHandle(String handle)

/** switches focus of the webdriver to the first window that is not the current parent window*/
public void switchWindow(String parent)

/** switches focus to the specified frame including a wait for the frame to be available*/
public void switchFrame(String frameLocator)

/** switches focus to the specified frame including a wait for the frame to be available*/
public void switchFrame(By by)

/** switches focus to the specified frame including a wait for the frame to be available*/
public void switchFrame(Element el)

/** switches focus to the top window or first frame*/
public void switchToDefaultContent()
```



The page objects of the test project should include:

- Selenium locators which define how to find the elements of the page within the DOM.
- Methods that perform selenium actions on the page (by re-using the BasePO methods wherever possible)



For example a very simple page object may look like:

```java
public class Login extends BasePO{
private By usertype = By.id("utype");
private By username = By.id("uname");
private By password = By.id("pword");
private By submitBtn = By.id("submit_1")
    
protected void login(String type, String user, String pass){
	waitPageToLoad();
    findElement(usertype).dropdown().selectByVisibleText();
    findElement(username).sendKeys(user);
	findElement(password).sendKeys(pass);
	findElement(submitBtn).clickable().click();        
    }
}
```

Using the built-in methods of the *BasePO* class ensures that the test code waits automatically for element readiness before performing selenium actions (helping to avoid flaky tests) and provides access to a number of in-built methods to simplify test code.

For example the above code used the following in-built methods of the *BasePO* class:

- waitPageToLoad() which checks page level readiness
- findElement() which waits for the element to be present in the DOM before proceeding

A number of these methods, such as findElement(), return an *<a name="element">Element</a>* object of the framework which wraps around Selenium's WebElement and includes a number of additional helper methods.

In the page object example above the dropdown(), sendKeys() and clickable() methods of the *Element* class are being used.  These methods perform actions such as creating the Selenium Select object needed to interact with dropdown fields or waiting for a field to become clickable.



Selenium's PageFactory style of writing page objects can also be used. The locators should be written using the  `@FindBy` style notation and a constructor method added to the page object to invoke the initialise() method of the *BasePO* to instantiate the page factory.  

Using the same example as above:

```
public class Login extends BasePO{
	@FindBy(how=How.ID, using = "utype") private WebElement usertype;
	@FindBy(how=How.ID, using = "uname") private WebElement username;
	@FindBy(how=How.ID, using = "pword") private WebElement password;
	@FindBy(how=How.ID, using = "submit_1") private WebElement submitBtn;
	
	Login(){
        initialise(this);
	}
    
    protected void login(String type, String user, String pass){
    	waitPageToLoad();
        Select sel = new Select(usertype);
        sel.selectByVisibleText();
        username.sendKeys(user);
        password.sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();        
    }
}
```

<br>

##### <a name="page-objects-codeless">8.5 JSON or Excel Page Objects (codeless)</a>

Page objects can also be created in either JSON or Excel format rather than in java code and still be invoked via Cucumber features and step definitions.

For complex applications coded page objects will provide more flexibility and are the recommended approach, however, for simple applications then either coded or codeless page objects can be adopted depending on the test projects preference.

The codeless page objects contain 2 parts:

- list of page object locators held in a JSON file (multiple objects) or Excel tab (multiple rows)
- list of page object methods held in a JSON file (multiple objects) or Excel tab (multiple rows)

Each page object locator includes:

- field name (any functional name)
- selenium locator method (from id, name, linkText, partialLinkText, className, tagName, css, xpath)
- the value of the locator

Each page object method contains one or more operations, where each operation includes:

- object name (cross reference to json file / excel tab holding a list of locators)
- field name (cross reference to the field name)
- action (see below)
- type (see below)
- value (the data)



| Action   | Type             | Description (aka the framework operation performed)          |
| -------- | ---------------- | ------------------------------------------------------------ |
| sendKeys | n/a              | element.sendKeys(value)                                      |
| click    | n/a              | element.click()                                              |
| dropdown | selectByText     | element.dropdown().selectByVisibleText(value)                |
| dropdown | selectByIndex    | element.dropdown().selectByIndex(value)                      |
| dropdown | selectByValue    | element.dropdown().selectByValue(value)                      |
| assert   | text             | assertEquals(element.getText(), value)                       |
| assert   | attribute name** | assertEquals(element.getAttribute(attribute name), value)    |
| assert   | selectedOption   | assertEquals(element.dropdown().getFirstSelectedOption(), value) |
| assert   | visible          | assertEquals(el.element().isDisplayed(), Boolean(value))     |
| assert   | enabled          | assertEquals(el.element().isEnabled(), Boolean(value))       |
| assert   | selected         | assertEquals(el.element().isSelected(), Boolean(value))      |
| wait     | page             | waitPageToLoad()                                             |
| wait     | visible          | wait.until(ExpectedConditions.visibilityOf(element))         |
| wait     | clickable        | wait.until(ExpectedConditions.elementToBeClickable(element)) |
| wait     | text             | wait.until(ExpectedConditions.textToBePresentInElement(element,value)) |

** the name of any attribute who's value is to be validated



The following example shows a simple excel based configuration with 2 page objects covering multiple  methods and a mixture of selenium actions.

![](documentation\readme\ExcelPO.png)



The following example shows a simple JSON based configuration with 2 page objects covering multiple methods and a mixture of selenium actions.

![](documentation\readme\JSONPO.png)



When using either the JSON or Excel approach the methods and locators can be defined in a one or more files and the file / tab naming is arbitrary since these are specified in the Cucumber steps.  

An example of invoking JSON Page objects is shown below:

```
@When("^a trainee registers$")
public void checkPageDisplay(Map<String, String> map) throws Throwable {
	autoFillCheckJSON("HealthConference", "register", map);
}

@Then("^a confirmation message is displayed$")
public void checkTravelDetails(Map<String, String> map) throws Throwable {
	autoFillCheckJSON("HealthConference", "check registration result", map);
}
```



An example of invoking Excel Page objects is shown below:

```
@When("^the journey page is displayed correctly$")
public void checkPageDisplay() throws Throwable {
	autoFillCheckExcel("HealthConference", "check journey page display", null);
}

@When("^a conference delegate checks their travel details$")
public void checkTravelDetails(Map<String, String> map) throws Throwable {
	
	autoFillCheckExcel("HealthConference", "add travel details", map);
	if (map.get("journeyType").equalsIgnoreCase("car")) {
		autoFillCheckJSON("HealthConference", "add travel details car", map);
	}else if(map.get("journeyType").equalsIgnoreCase("bike")) {
		autoFillCheckJSON("HealthConference", "add travel details bike", map);
	}else {
		autoFillCheckJSON("HealthConference", "add travel details public", map);
	}
}

@When("^the correct journey information will be calculated$")
public void checkJourneyDetails(Map<String, String> map) throws Throwable {
	autoFillCheckExcel("HealthConference", "check journey details", map);
}
```



The step definitions code invokes the codeless page object by using either the autoFillCheckJSON() or autoFillCheckExcel() methods of the BaseSteps class.  These methods accept the following parameters:

- name of the json or excel file that includes the page object methods (file name only without extension)
- name of the method to be invoked
- any test data to be used (in map<String, String> format)

The autoFillCheck() logic will read the file and iterate through the json object / excel data rows for the test method and sequentially execute the included selenium operations.

If the value field/column within the json or excel page object method is populated with a literal value (e.g. "foo") then this will be used within the selenium action.  If the value field/column is wrapped in *<>* (e.g. \<foo\>) then the actual value will be substituted from the data map object that is passed in from the step definition.  This map object could be passed into the step definition from the cucumber scenario or be created from an external data source.

![](documentation\readme\CodelessPOData.png)

 

##### <a name = "test-assertions">8.6 Test Assertions</a>

It is recommended that all test assertions are performed within the step definition methods rather than within page objects.  This helps to create a clear definition of responsibilities across the steps and page object layers of the test code:

- Steps coordinate the test flow, invoke page object methods, and perform "tests" (aka assertions)
- Page objects interface with the application and perform the "heavy lifting" selenium actions

Any assertion library such as Hamcrest or AssertJ can be used within the test code by adding the dependency to the test project Build.gradle file, however, for most situations it is recommended that the in-built TestNG assertions are used.

TestNG provides *hard assertions* and *soft assertions*.  Both include the same set of assertion checks (e.g. equals, notEquals, null et al). With hard assertions as soon as a single check fails then an exception will be thrown and the test will fail and stop.  With soft assertions it is possible to chain together multiple checks and report the aggregate result across the checks.

Hard assertions can be applied as:

```
import org.testng.Assert;
....
Assert.assertEquals(<actObject1>, <expObject1>);
Assert.assertEquals(<actObject2>, <expObject2>);
```

Soft assertions can be applied as:

```
import org.testng.asserts.SoftAssert;
....
SoftAssert sa = new SoftAssert();
sa.assertEquals(<actObject1>, <expObject1>);
sa.assertEquals(<actObject2>, <expObject2>);
sa.assertAll();
```

To further support soft assertions the framework ThreadContext class already includes a TestNG SoftAssert object for each running thread.  This removes the need to import and instantiate the SoftAssert and also allows soft asserts to be aggregated across step methods.  In addition, the framework includes a fail safe assertAll() statement within the after hooks of the test against this ThreadContext assert object.

To use this built-in soft assert within any test code:

```
ThreadContext.getInstance().sa().assertEquals(<actObject1>, <expObject1>);
ThreadContext.getInstance().sa().assertEquals(<actObject2>, <expObject2>);
ThreadContext.getInstance().sa().assertAll();
```



##### <a name="element">8.7 Element</a>

The *Element* class wraps around the Selenium WebElement object.  It includes the *By* locator, the *WebElement* itself, and a *WebDriverWait* object as instance variables along with a set of helper methods for operating on web element.  

Typically an instance of the class will be created whenever one of the *findElement()* methods of the *BasePO* class is used.  However, it also includes constructor methods that allow it to be instantiated directly passing either a *By* locator or *WebElement*.

The following framework methods are available within the *Element* class:

```
/** constructor accepting WebElement */
public Element(WebElement e)

/** constructor accepting By locator. Performs wait for element to exist in DOM */
public Element(By by, int...delay)

/** constructor accepting ExpectedCondition. Performs wait based on the specified condition */
public Element(ExpectedCondition<?> exp, int...delay)

/** returns the By locator that was used to find the Element */
public By by()

/** returns the WebElement object */ 
public WebElement element()

/** performs wait for the element to become clickable */
public Element clickable()

/** returns inner text of the element */
public String getText()

/** returns text held in the attribute "value" */
public String getValue()

/** returns text held in the specified attribute */
public String getAttribute(String attr)

/** clears the value held in the element and returns itself */
public Element clear()

/** populates the element with the specified value and returns itself */
public Element sendKeys(String val)

/** clicks the element and returns itself */
public Element click()

/** performs a javascript click on the element and returns itself */
public Element clickJS()

/** sets the "value" attribute of the element with the specified value using javascript and returns itself */
public Element sendKeysJS(String val)

/** uses Keys.chord(Keys.CONTROL, "a") to overwrite the value in the element with the specified string value */
public Element sendKeysChord(String val)

/** uses Keys.chord(Keys.CONTROL, "a") to overwrite the value in the element with the specified key sequence */
public Element sendKeysChord(Keys key)

/** performs action of hitting the enter key */
public Element sendEnter()

/** performs click action on checkboxes / radio buttons if current selected state different to specified boolean value*/
public Element select(Boolean val)

/** performs click action on checkboxes / radio buttons where state is not currently selected*/
public Element select()

/** performs click action on checkboxes / radio buttons where state is currently selected */
public Element unselect()

/** Return all inner text within a list of elements as string array */
public List<String> getAllText(List<Element> els)

/** Performs mouse action move to the element on the screen */
public Element move()

/** Performs mouse action to click and hold the element*/
public Element clickAndHold()

/** Performs mouse action to release a hold */
public Element release()

/** Performs mouse action move to the element, locate a child element and click */
public Element moveAndClick(WebElement elChild)

/** searches again for the element using the elements By locator */
public Element refind()

/** Returns a nested element including wait for element to exist in DOM */
public Element findElement(By by)

/** Returns list of nested elements */
public List<Element> findElements(By by)

/** Returns a selenium select object for the Element */
public Select dropdown()
	
/** Return all options within a dropdown as string array */
public List<String> getDropdownOptionsText()

/** Return all options groups within a dropdown as string array */
public List<String> getDropdownOptGroupsText()

/** Return all options groups within a dropdown as list of elements */
public List<WebElement> getDropdownOptGroupsElements()

/** Return all options within an option group of a dropdown as string array */
public List<String> getDropdownOptionsTextWithinGroup(String group)

/** Return all options within an option group of a dropdown as list of elements */
public List<WebElement> getDropdownOptionsElementsWithinGroup(String group)

/** Highlights an element with a 3px blue border.....useful when debugging/taking screenshots */
public Element highlight()
```



##### <a name="webdriver-management">8.8 WebDriver Management</a>

The framework includes *DriverFactory*, *DriverManager*, and *Capabilities* classes which automatically handle starting, configuring and closing the WebDriver instance(s) across all tests.  ThreadLocal driver management is used within this factory to enable parallel test execution.  

The *Hooks* class within the *cf_selenium_utils* module manages the WebDriver lifecycle for all tests that are executed.

Test projects do not need to write or perform any additional driver management.  The webdriver for each running thread is directly available as a variable named *driver* to any test classes that extend either the *BaseSteps* or *BasePO* classes of the cf_selenium_utils module.  

Tests can be configured to run against:

- local instances of Firefox, Chrome, Internet Explorer, Edge, Safari, PhantomJS, HTMLUnit
- remote selenium grid against any browser+OS configured on the grid
- remote SauceLabs or BrowserStack against any browser+OS available on these services

The driver factory of the framework includes a number of classes that extend the abstract *Driver Manager* class and which include the code needed to configure each specific webdriver.  

A test project simply pre-defines json configuration file(s) specifying one or more target server/browser/os combinations for a test and then passes the name of the config file as a runtime parameter when executing the tests.

The example below shows a configuration file defining local test execution against chrome:

```
[{
			"seleniumServer":"local",
			"browserName":"chrome"
}]
```

The example below shows a configuration file defining parallel test execution against saucelabs on 3 browser / OS configurations:

```
[{
			"seleniumServer":"saucelabs",
			"browserName":"firefox",
			"version":"54",
			"platform":"Windows 10"
},{
			"seleniumServer":"saucelabs",
			"browserName":"chrome",
			"version":"48",
			"platform":"Linux"
},{
			"seleniumServer":"saucelabs",
			"browserName":"firefox",
			"version":"55",
			"platform":"Linux"
}]
```

The example below shows a configuration file defining parallel test execution against browserstack on 2 browser / OS configurations:

```
[{
			"seleniumServer":"browserstack",
			"browser":"firefox",
			"browser_version":"54",
			"os":"Windows",
			"os_version":"10"
},{
			"seleniumServer":"browserstack",
			"browser":"chrome",
			"browser_version":"48",
			"os":"Windows",
			"os_version":"10"
}]
```

Each server/browser/os combination (i.e. JSON object) within the configuration file results in a separate thread of execution.

When instantiating the WebDriver for remote grid/saucelabs/browserstack execution the framework sets the Desired Capabilities of the driver for the execution thread based on the list of key  / value pairs defined within the json object.  

Any number of key / value pairs can be included in the JSON configuration file.  This allows the desired capabilities to be specified as required by the remote service (e.g. saucelabs requires 'platform' whist browserstack requires 'os' and 'os_version').

In other words the driver factory loops through each server/browser/os combination defined in the configuration file and for that execution thread performs following statement:

```
DesiredCapabilities.setCapability(<key>, <value>)
```

For local execution Selenium requires access to browser driver binaries (e.g. ChromeDriver, GeckoDriver, IEDriverServer).  

The test project can provide these manually by downloading and including the required drivers in the */lib/drivers/* folder of the test project.   

Alternatively, the framework integrates the open source WebDriverManager library into the Driver Factory.  By setting the runtime parameter `cukes.webdrivermanager=true` the framework will use WebDriverManager to automatically download the appropriate driver binary for the OS / Browser when running a test. This particularly useful given the frequency at which Browsers versions are updated and driver binaries become superseded. Note that the drivers are cached and therefore re-download only occurs as and when needed.  

##### <a name="selenium-waits">8.9 Selenium Waits</a>

In line with general selenium best practice it is recommended that test projects adopt 'fluent ' or 'explicit' waits rather than 'implicit' waits when writing test code.  

A number of waits are automatically built into the "find" methods of the framework, but to support additional wait actions within a test project a selenium *WebDriverWait* object is directly available as a variable named *wait* to any test classes that extend either the *BaseSteps* or *BasePO* classes of the *cf_selenium_utils* module.  

The framework has a configurable wait time (in milliseconds) that is used by this wait object and which can be set in the *src/test/resources/config/selenium/runtime.properties* file of the test project. By default this is set to 10: `defaultWait=10`.

Example usage:

```
wait.until(ExpectedConditions.<somewaitcondition>(<some element or by locator>);
```



##### <a name="screenshots">8.10 Screenshots</a>

By default the framework will automatically capture screenshots on test failure and embed these into the html output report.  

It is possible to turn off the screenshots by setting the parameter `screenshotOnFailure=true` in the src/test/resources/config/selenium/runtime.properties file of the test project.

Screenshots can also be captured and included in the output report at any point in the test code by invoking either the *addScreenshot()* method of the *BaseSteps* class, or the static methods provided in the *Screenshot* class and the *Reporter* class.  

Example usage:

```
addScreenshot();
```

or

```
import static uk.ndc.csa.utilities.common.Reporter.*;
import static uk.ndc.csa.utilities.selenium.screenshot.Screenshot.*;
....
File file = saveScreenshot(grabScreenshot(), getScreenshotPath());
String relativePath = "." + File.separator + "Screenshots" + File.separator + file.getName();
addScreenCaptureFromPath(relativePath);
```

The framework has a configurable delay (in milliseconds) before taking screenshots which can be set in the src/test/resources/config/selenium/runtime.properties file of the test project. By default this is set to 0: `screenshotDelay=0`.

##### <a name="selenium-runtime-properties">8.11 Selenium Runtime Properties File</a>

The *config/selenium/* folder holds the *runtime.properties* file used by the framework for browser based selenium execution.  The following property values can be set.

| Attribute                          | Description                                                  |
| ---------------------------------- | ------------------------------------------------------------ |
| *defaultWait*                      | default wait time when finding WebElement using selenium fluent waits (value in seconds - set to 10 by default). |
| *screenshotDelay*                  | value in seconds used for delay before taking screenshots (value in seconds - set to 0 by default) |
| *screenshotOnFailure*              | specifies whether screenshots should be automatically captured for test failures (boolean true or false) |
| *clickUsesJavaScript.[browser]*    | specifies by browser whether javascript click should be used in place of native selenium click |
| *sendKeysUsesJavaScript.[browser]* | specifies by browser whether javascript value setting should be used in place of native selenium sendKeys |
| *scrollToElements.[browser]*       | specifies by browser whether scrolling to an element should be invoked before performing actions on that element (boolean true or false) |
| *desiredCapabilities.[browser]*    | sets any desired capabilities that a test project wants to be apply for a given browser driver. Multiple entries can be included for each browser. |
| *options.[browser]*                | Sets any driver options that a test project wants to be applied for a given browser.  Multiple entries can be included for each browser. |

An example of a selenium.properties file is given below:

```
######Waits and Screenshot######
defaultWait=10
screenshotDelay=0
screenshotOnFailure=true

######JavaScript Actions######
clickUsesJavaScript.internetexplorer=true
sendKeysUsesJavaScript.internetexplorer=false

######Scroll to Elements######
scrollToElements.chrome=true

######Desired Capabilities######
desiredCapabilities.internetexplorer=ignoreProtectedModeSettings==true
desiredCapabilities.internetexplorer=ignoreZoomSetting==true
desiredCapabilities.internetexplorer=requireWindowFocus==false

######Driver Options######
options.chrome=--start-maximized
```

[back to top](#home)

<br>

<br>

### <a name="hooks">9. Adding Hooks</a>

The framework already includes a number of TestNG and Cucumber hooks in the cf_common_utils, cf_selenium_utils and cf_jira_utils modules that perform the actions necessary to start-up/tear-down the framework execution, to run each individual Cucumber Feature, to update test reports, to capture screenshots on test failure and to optionally create test bugs in JIRA.

These framework hooks are ordered such that the cf_common_utils hooks are the first and last to be executed. 

- Before test executes: cf_common_utils hooks -> cf_selenium_utils hooks
- After test executes:  cf_selenium_utils hooks -> cf_jira_utils hooks -> cf_common_utils hooks

Normal TestNG hook ordering applies by including an `(order=<value>)` parameter for the @Before or @After annotation.  @Before hooks are executed based on lowest order value first, whilst @After hooks are executed based on lowest order value last.

The *cf_common_utils* hooks therefore have an order=10, *cf_jira_utils hooks* have an order=20 and *cf_selenium_utils* hooks have an order=30.

It is also possible for test projects to include their own test specific hooks by adding a *Hooks* class in the *src/test/java/hooks* package of the project.

Order numbers 1-49 are reserved for use by the framework itself and therefore test project hooks should have an order value of 50 or greater.  The example below illustrates this:

```
public class Hooks {

	@Before(order=50)
	public void startUp(Scenario scenario) {  
		//test project code goes here
	}

	@After(order=50)
	public void closeDown(Scenario scenario) {  
		//test project code goes here
	}
}
```

[back to top](#home)

<br>

<br>

### <a name="running-tests">10. Running Tests</a>

The tests can be executed from:

- a CI tool such as jenkins by executing the gradle 'cukes' task defined in the gradle.build file or by running the maven 'test' phase (which is part of the standard Maven Build Lifecycle)
- the command line using the gradle 'cukes' task defined in the gradle.build file or by running the maven 'test' phase (which is part of the standard Maven Build Lifecycle)
- the command line by running a pre-built Jar for the test project
- an IDE (such as Eclipse or IntelliJ) using TestNG

The framework accepts a number of mandatory and optional runtime parameters which can be set as either system properties or environment variables (see [Runtime Parameters](#runtime-parameters)).

To execute a suite of api tests from the command line passing in the mandatory parameters for API testing :

```
gradle cukes -Dcukes.env=someenvironment -Dcukes.testsuite=somesuite 
or
mvn test -Dcukes.env=someenvironment -Dcukes.testsuite=somesuite 
```

To execute a suite of browser tests from the command line passing in the mandatory parameters for browser testing:

```
gradle cukes -Dcukes.env=someenvironment -Dcukes.browsercombo=somebrowsercombo  -Dcukes.testsuite=somesuite -Dcukes.selenium=true
or 
mvn test -Dcukes.env=someenvironment -Dcukes.browsercombo=somebrowsercombo  -Dcukes.testsuite=somesuite -Dcukes.selenium=true
```

In the command line execution statements above the values given for the parameters have the following meanings:

*\<someenvironment\>* equals the name of environment property file held in folder src\test\resource\config\environments\

*\<somebrowsercombo\>* equals the name of a browser config file (excluding '.json' extension) held in folder src\test\resource\selenium\stacks\

*\<sometestsuite\>* equals the name of a TestNG suite (excluding '.xml' extension) held in folder src\test\resource\testsuites\

For Jenkins execution a predefined Jenkins Pipeline job has been scripted and can be re-used within any test project Jenkins instance - see the section [Jenkins Integration](#jenkins-integration).  

The Jenkins job is parameterised to accept the mandatory runtime parameters shown above.  Further optional runtime parameter values can also be set/exported within the Jenkins job.

If tests are being executed locally from within an IDE such as Eclipse or IntelliJ then the runtime parameters can be set within a standard TestNG run configuration as VM arguments or Environment Variables.  The TestNG run configuration can also be set to run any individual 'runner class' or 'testsuite'.

Optionally the test project can be precompiled/built into a fat Jar.  Section 3.2 outlines a gradle task that can be included in the test projects build.gradle file to support this.  The tasks can then be executed as:

`gradle fatJar`

The jar will be written to the *build/libs/* folder of the project.  The jar can be executed as:

`java <list of system properties> -jar <jar file> <test suite>`

For example:

```
java -Dcukes.webdrivermanager=true -Dcukes.jira=true -Dcukes.env=devtest -D cukes.browsercombo=LOCAL_CH -jar HealthConferenceTest.jar browsertests.xml
```

Note that the src/test/resources/config folder of the test project needs to be packaged alongside the jar.

[back to top](#home)

<br><br>

### <a name="runtime-parameters">11. Runtime Parameters</a>

The framework uses the following mandatory runtime parameters for all tests:

| Property          | Description                                                  |
| ----------------- | ------------------------------------------------------------ |
| *cukes.env*       | defines the name of the environment to be used for the test run.     Maps to the name of an environment property file under src/test/resources/config/environments/ |
| *cukes.testsuite* | defines the test suite to be executed for the test run.                            Maps to the name of a test suite file under src/test/resources/testsuites/ |

The framework uses the following additional runtime parameters when running selenium tests:

| Property                                                     | Description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| cukes.selenium                                               | [mandatory] instructs the framework's driver factory to automatically manage the start-up and tear-down of the Selenium WebDriver for each Cucumber test scenario. |
| cukes.browsercombo                                           | [mandatory] defines the OS+browser combination to be used when running the test |
| cukes.webdrivermanager                                       | [optional] If set to true then framework will use the WebDriverManager library to automatically download a browser driver binary to match the OS + Browser combination.  If set to false (or not set) then will use a project defined driver based on the cukes.driverPath property |
| cukes.driverPath                                             | [optional] If populated the framework will use this path to find the required driver for the OS+Browser combination.  If not set then will default to the project folder: /lib/drivers/ |
| cukes.jira                                                   | [optional] If set to true then will attempt to create bugs on test failure against the project jira instance (as configured under src/test/resources/jira/) |
| cukes.jira.bugLevel                                          | [optional]<br>0= no bugs raised <br>1= single bug raised for the overall run<br>2= individual bug raised per failed scenario |
| cukes.jira.featuresPath                                      | [optional] path used to save '.feature' files when running bulk export from JIRA |
| cukes.seleniumGrid                                           | [optional] Defines the end point for running tests against a selenium grid.  Framework automatically uses this and connects to the grid when the `cukes.browsercombo` indicates that grid based execution to be used. |
| cukes.saucelabsUserName<br>cukes.saucelabsAccessKey<br>cukes.saucelabsEndPoint | [optional] Defines the end point and credentials for running tests against saucelabs. Framework automatically uses this and connects to saucelabs when the `cukes.browsercombo` indicates that saucelabs based execution to be used. |
| cukes.browserstackUserName<br>cukes.browserstackAccessKey<br>cukes.browserstackEndPoint | [optional] Defines the end point and credentials for running tests against browserstack. Framework automatically uses this and connects to browserstack when the `cukes.browsercombo` indicates that browserstack based execution to be used. |
| cukes.JOB_NAME<br>cukes.BUILD_NUMBER                         | [optional] Defines the optional values to be passed to Saucelabs/Browserstack (for reporting only) |

Note: the runtime parameters `cukes.env`, `cukes.testsuite` and `cukes.browsercombo` require only the file name without the '.properties', '.xml', or '.json' extensions.  The paths to these files are taken from the src/test/resources/paths.properties config file.

All of the above parameters can be set as java system properties or environment variables.  The framework will first check for a java system property value and use this if set.  If no system property exists it will then check for an environment variable value.  

For example setting the test suite from the command line as a java system property: 

`-Dcukes.testsuite=integrationsuite`

[back to top](#home)

<br><br>

### <a name="output-reporting">12. Output Reporting</a>

The framework incorporates a 'listener' that monitors the cucumber test results and automatically generates a rich html output report using the ExtentReports library (without the need for any logging statements in the test project code).  

This report presents the run results broken down by Cucumber Features, Scenarios, Steps and Tags including aggregate graphical and statistical results plus drill downs to detailed results.

Where [parallel test execution](#parallel-test-execution) or [multi-browser testing](#multi-browser-tests) is performed a single report is automatically generated including the results from each execution thread.

Where browser and api tests are executed in the same run then a single report is automatically generated including the results from all tests.

Where assertion tests fail then the expected and actual results are included in the report, and for failing browser tests screenshots are automatically captured and embedded.

In addition a simple tabular text report is also generated that summarizes the test run and lists out the features/scenarios that are passed or failed.  This text report is printed to the console at the end of each run.  

The text report can also be saved to a flat file (e.g. to use in Jenkins Email Notifications) by including an optional entry in the paths.properties file to specify the location  to write the file, such as: `textReportPath=./RunResults/results.txt`

<br>

##### <a name="html-report">12.1 HTML Output Report (ExtentReport)</a>

The following extracts from an actual test run output report highlight the html reporting functionality.

- Graphical / Statistical Summary

![](readme/Report_1.png)



- Features Summary View

![](readme/Report_2.png)



- Scenario Detail Views

![](readme/Report_3.png)

![](readme/Report_4.png)





As well as the standard report entries that the framework creates it is possible to include additional custom entries from within the test project code.

To add additional messages into the list of step messages produced for a scenario:

```
Reporter.addStepLog("some message");
```

To add messages to an additional *TestRunner Logs* tab in the report:

```
Reporter.setTestRunnerOutput("some message");
or
List<String> list = some list of string messages
Reporter.setTestRunnerOutput(list);
```



Custom messages added into the report can be simple text or can be formatted with basic html tagging.  

For example the following code generates the output below. 

```
Reporter.addStepLog("here is some step output:"+"<br>");
StringBuilder str = new StringBuilder();
str.append("<table>");
str.append("<tr>");str.append("<th>Month</th>");str.append("<th>Savings</th>");str.append("<th>Balance</th>");str.append("</tr>");              
str.append("<tr>");str.append("<td>January</td>");str.append("<td>100</td>");str.append("<td>250</td>");str.append("</tr>");
str.append("</table> ");
Reporter.addStepLog(str.toString());
```

  

![img](readme/ReportLog.png)

<br>

##### <a name="html-report-flatFile">12.2 Flat File Text Report</a>

The following extract shows an example text report output:

```
Run Result Summary
------------------------------------------------------
| Status     | Passed  | Failed  | Skipped | Other   |
------------------------------------------------------
| Features   | 0       | 3       | 0       | 0       |
| Scenarios  | 1       | 3       | 0       | 0       |
------------------------------------------------------

Run Result Listing
-------------------------------------------------------------------------------------------
| Feature -> Scenario                                          | Status | Duration        |
-------------------------------------------------------------------------------------------
| Delegate Registration                                        | fail   | 0h 0m 16s 285ms |
|   -> Register for Event (test 1)                             | fail   | 0h 0m 10s 795ms |
| Delegate Registration Auto                                   | fail   | 0h 0m 17s 506ms |
|   -> Register for Event (test 2)                             | fail   | 0h 0m 12s 85ms  |
| Delegate Journey Planner Auto                                | fail   | 0h 0m 20s 13ms  |
|   + Scenario Outline: Check Journey <start> by <travel type> | n/a    | n/a             |
|   -> Check Journey Manchester, UK by car                     | pass   | 0h 0m 8s 204ms  |
|   -> Check Journey Bath, UK by bike                          | fail   | 0h 0m 6s 419ms  |
-------------------------------------------------------------------------------------------
| Started: Wed Jun 13 21:04:51 BST 2018                                   0h 0m 37s 615ms |
-------------------------------------------------------------------------------------------

Simple text report saved to: ./RunReports/results.txt
Full Html Run Report (ExtentReport) saved to: ./RunReports/RunReport_20180613_210528.html
```

- Lines beginning with ` -> ` markers are the individual cucumber scenarios
- Lines beginning with ` + ` markers are the start of a scenario outline 
- Lines without either of the above markers are the cucumber features
- The bottom row provides the start time as well as the duration time of the overall run.

[back to top](#home)

<br>

##### <a name="html-report-email">12.3 Email Report (Jenkins Pipeline)</a>

To do an email report, you will need to enable the Flat File Text Report, Email Extend Jenkin plugin and Jenkins readFile plugin. Flat File Text Report is used for the bases of the email content. Please see section [12.2 Flat File Text Report](#html-report-flatFile).

You can then add a step in the Jenkins Pipeline that sends an email report to selected people. See below an example step that can be used to send an email report. 

```
stage('Email Report') { 
  def fileContext = readFile "path to report/report.txt"
  def resultTable = '<pre style="font: monospace">' + fileContext + '</pre>'
  
  def header1 = "<h2>An automated test was triggered!</h2>"
  def header2 = "<h4>JOB: ${JOB_NAME} - ID ${BUILD_NUMBER}</h4>"
  

  def buildLink = "<p>Job URL: ${BUILD_URL}</p>"
  def signature = "<br>Regards,<br><h3>Accenture Open Source Test Automation Framework</h3>"
  
  def html = header1 + header2 + buildLink + resultTable + signature
  def emailsubject = "Cucumber Report"
  
  emailext body: html, recipientProviders: [developers(), requestor()], replyTo: 'noreplay@testing.lcl', subject: emailsubject
}
```


- Update the path in fileContext to read from your report location within the jenkins workspace area
- To modify the email subject, modify the emailsubject variable
- To modify the email body, modify the html variable which is built up from `header1 + header2 + buildLink + resultTable + signature`. `resultTable` is the variable for storing the text report and should not be altered.
- To modify the email recipient or email settings please see the Jenkins emailext pipeline syntax


Example email:

![](documentation\readme\emailreport.png)

[back to top](#home)

<br><br>

### <a name="parallel-test-execution">13. Parallel Test Execution</a>

The framework supports running tests (aka Cucumber features) in parallel to reduce overall execution time.    

To do this it leverages TestNG's standard test suites functionality.  As described in the [Test Suites](#test-suites) section of this document one or more test suites can be defined and included in the */src/test/resource/testsuites/* folder of a test project.  The additional **suite** element within the xml file also allows granular control over the sequential / parallel execution of the tests via an additional 'parallel' attribute.  An example is shown below:

```
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="suite1" parallel="classes" thread-count="10" data-provider-thread-count="10">
```

<br>

- if the parallel attribute is excluded then single threaded execution will be invoked and all tests will execute sequentially and within each test the runner classes and their associated features will execute sequentially
- if the attribute is set `parallel="tests"` then multi-threaded 'test' execution will be invoked. A thread of the framework will be spawned for each test defined in the suite and these tests will run in parallel.  Within each test thread the runner classes and their associated features will execute sequentially.
- if the attribute is set to `parallel="classes"` then multi-threaded 'class' execution will be invoked.  The tests defined within the suite will run sequentially but within each of these tests a parallel thread of the framework will be spawned for each runner class it includes and therefore the runner classes and their associated features for each test will be executed in parallel.

For example in the following test suite xml file, parallelisation is defined at the class level.  The test named "test1" executes first followed sequentially by the test named "test2" and finally by "test3".  However, when executing "test1" the runner classes "FeatureRunner1", "FeatureRunner2", and "FeatureRunner3" and their associated cucumber features will be executed in parallel using multi-threading.  The same applies to "test3" with "FeatureRunner5" and "FeatureRunner6" executing in parallel.

```
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="suite1" parallel="classes" thread-count="10" data-provider-thread-count="10">
    <test name="test1">
        <classes>
         	<class name="runners.FeatureRunner1"/>
         	<class name="runners.FeatureRunner2"/>
         	<class name="runners.FeatureRunner3"/>
        </classes>
    </test>	
    <test name="test2">
        <classes>
         	<class name="runners.FeatureRunner4"/>
        </classes>
    </test>	
    <test name="test3">
        <classes>
         	<class name="runners.FeatureRunner5"/>
         	<class name="runners.FeatureRunner6"/>
        </classes>
    </test>	
</suite>
```



![](readme/ParallelTests1.png)



Once the test suite .xml files are defined then ongoing test execution can flip-flop between them simply by varying the value of the runtime parameter *cukes.testsuite*.

[back to top](#home)

<br><br>

### <a name="multi-browser-tests">14. Multi Browser Tests</a>

The framework supports running the same tests (aka Cucumber Features) against multiple OS + Browser combinations.  To do this it leverages TestNG's in-built "data provider" functionality for parallel test execution.  

A test project simply defines the set of browser combinations to be tested in one or more JSON files which are held in the */src/test/resources/config/selenium/stacks/* folder.

The name of the JSON file(s) are arbitrary since the file to be used for a given run is specified as a [runtime parameter](#runtime-parameters) when executing the framework.  

The BaseTest class of the *cf_common_utils* module includes a data provider method that reads the JSON file and injects the set of browser combinations into the overarching @Test method within the BaseTest class which in turn spawns multiple framework threads for parallel browser execution.

For example the following JSON file would result in any tests being executed locally against both Chrome and Firefox in parallel (e.g. when running on a dev machine/laptop)

```
[
		{
			"seleniumServer":"local",
			"browserName":"chrome"
		},
		{
			"seleniumServer":"local",
			"browserName":"firefox"
		}
]
```



To execute against a selenium grid, Saucelabs or Browserstack the value of the seleniumServer attribute is changed and additional parameters defining the browser version, OS and OS version are added as needed.

For example the following JSON file would result in any tests being executed via BrowserStack against different combinations of Windows 10 / Linux and Chrome / Firefox / Internet Explorer.

```
[
		{
			"seleniumServer":"browserstack",
			"browser":"firefox",
			"browser_version":"54",
			"os":"Windows",
			"os_version":"10"
		},
		{
			"seleniumServer":"browserstack",
			"browser":"chrome",
			"browser_version":"48",
			"os":"linux",
			"os_version":"10"
		},
		{
			"seleniumServer":"browserstack",
			"browser":"internet explorer",
			"browser_version":"11",
			"os":"Windows",
			"os_version":"10"
		},
]
```



When the framework starts the cucumber execution it uses TestNG to spin up a separate thread for each browser combination.

Once these OS + browser combo JSON files are defined then ongoing test execution can flip-flop between them simply by varying the value of the runtime parameter *cukes.browsercombo*.

[back to top](#home)

<br><br>

### <a name="parallel-tests-and-multi-browser-combined">15. Parallel Tests and Multi Browser Combined</a>

The sections above describe Parallel Test execution and Multi-Browser execution.  These can be applied individually or be combined together.  This is illustrated in the following diagram:

![](readme/ParallelTests3.png)



[back to top](#home)

<br><br>



### <a name="jenkins-integration">16. Jenkins Integration</a>

A Jenkins pipeline job has been predefined and scripted for re-use by test projects.  This enables standalone execution of a test project or integration as part of an extended CI pipeline.

The required JenkinsFile is shown below.  

The JenkinsFile defines a simple parameterised pipeline job with 3 stages which will:

- accept the key runtime parameters `cukes.testsuite ` `cukes.environment` `cukes.browsercombo` as inputs
- stage 1 - clone the test project
- stage 2 - execute the tests using the `cukes` task that is defined in the build.gradle file
- stage 3 - publish the run report to Jenkins



To setup within Jenkins:

1. Create a new pipeline job (Menu->New Item->Pipeline)
2. In the pipeline section either choose "Pipeline script" and paste in the below script or choose "Pipeline script from SCM" and define the url/credentials of the GIT repository where your test project and JenkinsFile is held.
3. In the general section check the prepare an environment for the run and in the Properties Content box enter the values for any other runtime parameters you wish to set as a list of key value pairs (e.g. *cukes.jira=false*).



The pipeline job can then be executed using the *Build with Parameters* menu option within Jenkins.  Once the run has completed the run report will be attached to the Jenkins job with a link named *HTML Report*.

[An example Jenkins project is available for reference](http://34.252.181.171/jenkins/job/TestAutomationFramework/job/AutomationDemo/)

###### JenkinsFile:

```
properties([
  parameters([
    string(name: 'testsuite', description: '[mandatory] the name of the test suite (value should match name of a testng suite file (without .xml extension))' ),
    string(name: 'environment', description: '[mandatory] the target test environment (value should match name of environment property file)' ),
    string(name: 'browsercombo', description: '[optional] when running selenium tests the name of the browser configuration to be used (value should match name of a testng json config file)' ),
    ])
])

node {
    
   def baseDIR = '/var/jenkins_home/jobs/TestAutomationFramework/jobs/AutomationDemo/workspace/healthconference'
   stage('Clone Tests') { // for display purposes
    println 'echo grabbling git repository'
    git(
      	url: 'https://enterprise.id@innersource.accenture.com/scm/atac/cukesdemo.git',
       	credentialsId: '17399a96-9e17-4ac0-bcd6-c59ce8e8ceb8',
       	branch: 'master'
    	)	
    
   }
   stage('Run Tests') {
      sh 'echo removing old run reports'
      sh "rm -f ${baseDIR}/RunReports/*.html"
      sh 'echo running tests'
      sh "chmod 777 ${baseDIR}/gradlew"
      sh "${baseDIR}/gradlew -p${baseDIR} cukes -Dcukes.env=${environment} -Dcukes.browsercombo=${browsercombo} -Dcukes.testsuite=${testsuite}"
   }

   stage('Publish Results') {
        
    sh 'echo moving run report' 
    sh "mv ${baseDIR}/RunReports/*.html ${baseDIR}/RunReports/RunReport.html"
       
    publishHTML target: [
            allowMissing: true,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: "${baseDIR}/RunReports/",
            reportFiles: 'RunReport.html',
            reportName: 'HTML Report'
          ]  
   }
}
```



Note: the `baseDIR` variable and GIT credentials within the JenkinsFile will need amending by a test project.



The JenkinsFile shown above is also included at the root level of the [demo and template](#template-and-demo-projects) projects.

[back to top](#home)

<br><br>

### <a name="database-helper">17. Database Helper</a>

The *cf_database_utils* module of the framework includes 2 helper classes that can be re-used with a test project.

- DBUtilsHelper - connect and run DML or DDL SQL statements against a relational database
- MongoDBHelper - connect and run queries or updates against a MongoDB

Connection details can be added to the [environment properties](#environment-properties) file and will be picked up and used by the framework. The examples below show a Sybase and Oracle connection setting.

```
#Sybase
DBurl=jdbc:jtds:sybase://localhost:5030;autoCommit=false
DBdriver=net.sourceforge.jtds.jdbc.Driver	
DBusr=devenv
DBpwd=devenv
```

```
#Oracle						
DBurl=jdbc:oracle:thin:@localhost:1521:NPSDEV1
DBdriver=oracle.jdbc.OracleDriver	
DBusr=devenv
DBpwd=devenv
```

<br>

##### <a name="jdbc-database">17.1 DBUtilsHelper</a>

The *DBUtilsHelper* class includes the methods listed below.  

```
/** connects via JDBC using the url/usr/pwd and driver specified in the test project config */
public static void createConn()

/** connects via JDBC using the url/usr/pwd and driver supplied in input params */
public static void createConn(String url, String driver, String usr, String pwd)

/** closes the JDBC connection */
public static void closeConn()

/** runs SQL query statement returning single database row with columns as an object array */
public static Object[] getDBArray(String sql, Object...params)

/** runs SQL query statement returning multiple rows with columns as an object array */
public static List<Object[]> getDBArrayList(String sql, Object...params)

/** runs SQL query statement returning single row with columns as an object map */
public static Map<String, Object> getDBMap(String sql, Object...params)

/** runs SQL query statement returning multiple rows with columns as an object map */
public static List<Map<String, Object>> getDBMapList(String sql, Object...params)

/** runs SQL query statement returning single row as any POJO java class*/
public static <T> Object getDBBean(String sql, Class<T> clazz, Object...params)

/** runs SQL query statement returning multiple rows as list of any POJO java class*/
public static <T> List<T> getDBBeanList(String sql, Class<T> clazz, Object...params)

/** runs SQL insert/update/delete statement returning number of affected rows*/
public static int update(String sql, Object...params)

```

There is a single update method and 3 groups of read methods.   The update method returns the number of affected rows.  The read methods return:

1. array - either single array object for 1 row, or list of array objects for multiple rows
2. map - either single map object for 1 row, or list of map objects for multiple rows
3. pojo - either single pojo object for 1 row, or list of pojo objects for multiple rows

The read methods all operate the same way and will execute the same SQL.  The choice of which to use in your test code is based on which object type you would prefer for the returned result set.  Generally speaking either the map or pojo approaches will make the test code most readable since the keys of the map or the instance variables of the pojo match the database column heading names.

All of the update/read query types accept an optional list of parameters for substitution into the SQL statements (replaces any '?' in the SQL) .  

<br>

The following code snippets show some example usage.

*Read with single row returned into Object array of columns:*

```
String sql = "select * from myTable where myCol = 'foo'";
Obj[] rs = getDBArray(sql);

System.out.println(rs[0].toString());
System.out.println(rs[1].toString());
......
```

*Read with multiple rows returned into Map, where column headings are the key values:*

```
String sql = "select col1, col2, col3 from myTable where someColumn >= 1";
List<Map<String, Object> rows = getDBMapList(sql);

for (Map<String, Object> map : rows){
    System.out.println(map.get("col1"));
    System.out.println(map.get("col2"));
    System.out.println(map.get("col3"));
}
```

*Read with multiple rows returned into list of POJO's and with substitution variables:*

```
class myPOJO{
	public String col1;
	public String col2;
}
......
Object[] params = {1, "foo"};

String sql = "select col1, col2 from myTable where someColumn >= ? and anotherColumn = ?";
List<myPOJO> rows = getDBBeanList(sql, myPOJO.class, params);

for (myPOJO pojo : rows) {
	System.out.println(pojo.col1);
	System.out.println(pojo.col2);
}

```

*Update:*

```
String sql = "update myTable set myCol = 1";
int rows = update(sql);

System.out.println("number of rows updated = " + rows);
```

*Update with substitution variables:*

```
Object[] params = {"foo"};
String sql = "delete from myTable where some_id = ? ";
int rows = update(sql, params);

System.out.println("number of rows deleted = " + rows);
```

<br>

##### <a name="mongo-database">17.2 MongoDBHelper</a>

The MongoDBHelper class includes the methods listed below.

```
/** connects to MongoDB using the url/usr/pwd and database name specified in the test project config */
public static void createConn()

/** returns a collection from the database */ 
public static MongoCollection<Document> getCollection(String col)

/** closes the client connection */
public static void closeConn()

/** iterates through a list of collections and drops them from the database */
public static void dropCollection(List<String> collections)

/** inserts a single document into the collection */
public static void insertOne(String col, Document doc)

/** inserts multiple documents into the collection */
public static void insertMany(String col, List<? extends Document> docs)

/** runs the query and returns only one document as a string */
public static String findOne(String col, BasicDBObject query)

/** runs the query and returns a list of documents that match the conditions */
public static MongoIterable<Document> findMany(String col, BasicDBObject query)

/** returns a list of all the documents from a database collection */
public static MongoIterable<Document> getDoc(String col)

/** runs the query and updates a single document from the collection */
public static void updateOne(String col, BasicDBObject query, BasicDBObject updateObj)

/** runs the query and updates multiple documents from the collection */
public static void updateMany(String col, BasicDBObject query, BasicDBObject updateObj)

/** runs the query and deletes a single document from the collection */
public static void deleteOne(String col, BasicDBObject query)

/** runs the query and deletes multiple documents from the collection */
public static void deleteMany(String col, BasicDBObject query)
```



The following code snippets show some example usage.

*Java method to find one document using a JSONObject and verifying if expected value matches with the actual value in the document for a specified field:*

```
public void findOneDoc(String field, int value, JSONObject jsonObj) {
	Document doc = findOne("collection_name", andQuery(jsonObj));	
	Assert.assertEquals(value, doc.get(field));
}
```



*Helper method to iterate through one or more fields and construct an BasicDBObject:*

```
public static List<BasicDBObject> queryHelper(JSONObject jsonObj) {
	List<BasicDBObject> obj = new ArrayList<>();
	Iterator<?> keys = jsonObj.keys();
	while (keys.hasNext()) {
		String key = (String) keys.next();
		obj.add(new BasicDBObject(key, jsonObj.get(key).toString()));
	}
	return obj;
}
```

​	

*To create the query using an JSONObject:*

```
public static BasicDBObject andQuery(JSONObject jsonObj) {
	if (jsonObj != null) {
		BasicDBObject andQuery = new BasicDBObject();
		andQuery.put("$and", queryHelper(jsonObj));
		return andQuery;
	} else {
		return null;
	}
}
```



[back to top](#home)

<br>

<br>

### <a name="mocking-helper">18. Mocking Helper</a>

The *cf_mocking_utils* module of the framework includes a helper class and a small number of built-in Gherkin statements and Java Step Definitions that wrap around the WireMock library.  This is available to any test project that includes the *cf_mocking_utils* dependency.  

The built-in gherkin statements can be included in any Cucumber feature or the step definition methods can be invoked directly within the test code, such as a @Before or @BeforeClass hook.

A test project can start up a WireMock server and register multiple stubs (aka WireMock services) in 2 ways:

- defining the stub directly in the cucumber feature
- defining the stub in JSON configuration file(s)

Defining the stub directly in the feature is suitable for very simple services, however, the recommended approach is to define the stubs in JSON configuration files which provide greater control and flexibility for specifying the operation of the stub service.

<br>

*To start the WireMock server (and register any pre-defined stubs from json configuration files):*

```
Given the wiremock server is started
|127.0.0.1	|8081	|

@Given("^the wiremock server is started$")
public void startWireMock(List<String> mock)
```

See section below for details on pre-defining stubs via json.

<br>*To reset the WireMock server and clear any currently registered stubs:*

```
Given reset the wiremockservice

@Given("^reset the wiremockservice$")
public void resetWireMock()
```

<br>

*To close the WireMock server:*

```
Given close the wiremockservice

@Given("^close the wiremockservice$")
public void closeWireMock()
```

<br>

*To register a stub that is defined in a feature file datatable against the currently running WireMock server:*

```
Given a wiremock service is registered
|key			|value		|

@Given("^a wiremock service is registered$")
public void registerWireMockAPI(Map<String, String> param)
```

See section 18.1 below for details on defining stubs directly in the feature via a DataTable.

<br>

### <a name="json-stubs">18.1 Defining Stubs via Feature DataTable</a>

The Gherkin statement *Given a wiremock service is registered* enables a simple stub service to be defined directly within the feature file.

```
Given a wiremock service is registered
|action			|a valid rest action		|
|path			|the path for the api request	|
|request body		|a request body for matching	|
|response code		|a valid response code		|
|response header	|somekey:somevalue		|
|response body		|{a json object}		|
```

The *action*, *path* and *response code* values are mandatory, however, the *request body*, *response header*, and *response body* are optional and can be included or excluded from the table as needed. The following example shows the usage:

```
Given the wiremock server is started
|127.0.0.1	|8080	|
And a wiremock service is registered
|action			|GET					|
|path			|/users/details				|
|response code		|200					|
|response header	|Content-Type:application/json		|
|response body		|{"name":"foo bar"}			|
And a wiremock service is registered
|action			|POST					|
|path			|/users/create				|
|request body		|{"forename":"foo","surname":"bar"}	|
|response code		|201					|
|response header|Content-Type:application/json			|
And a wiremock service is registered
|action			|PUT					|
|path			|/order/update				|
|request body		|{"type":"foo","quantity":1}		|
|response code		|204					|
```

<br>

### <a name="json-stubs">18.2 Defining Stubs via JSON configuration files</a>

The Gherkin statement *Given the wiremock server is started* will start the WireMock server and will also call WireMock's methods to register any stub services that are defined and held as 1 or more json files within the */src/test/resources/mocks/mappings* folder of the test project.  

This uses the standard functionality of WireMock and therefore all the capabilities for defining WireMock stubs via JSON are available.

Note that the location of the */src/test/resources/mocks/* folder can be amended via the *mockServicePath* parameter in the *paths.properties* file, however, this must include a *mappings* sub-folder which is required by the WireMock library.

The following examples give some sample usage:

*A basic stub for a GET request:*

```
{
    "priority": 3,
    "request": {
        "method": "GET",
        "url": "/api/mytest"
    },
    "response": {
        "status": 200,
        "body": "this was read from simple json"
    }
}
```

*A basic stub for a POST request:*

```
{
    "priority": 3,
    "request": {
        "method": "POST",
        "url": "/api/mytest/create"
    },
    "response": {
        "status": 201,
        "body": "this was created from simple json"
    }
}
```

*A basic stub with POST request and field matching from the request body:*

```
{
    "priority": 1,
    "request": {
        "method": "POST",
        "url": "/account/create",
        "bodyPatterns" : [ {
      	"matchesJsonPath" : "$.[?(@.field2==555)]"
    	} ]
    },
    "response": {
        "status": 201,
        "body": "{\"field1\": \"POST with SUCCESS FOR A REQUEST with field2=555 in the body\"}"
    }
}
```

*A basic stub with POST request and parameter matching from the request:*

```
{
    "priority": 1,
    "request": {
        "method": "POST",
        "urlPathPattern": "/myAPI/do/something.*",
        "queryParameters" : {
      		"name" : {
        	"equalTo" : "foo"
      		}
      	}
    },
    "response": {
        "status": 201,
        "jsonBody": {"data":{"POST with SUCCESS FOR A REQUEST with query parameter name=foo"}},
        "headers": {
            "Content-Type": "application/json"
            }
    }
}
```



<br>

###### Registering the JSON Stubs via Hooks

As an alternative to starting the WireMock server and registering stub services via a Gherkin Statement in the feature, the same can also be achieved using Hooks and calling the step definition methods directly.

The below snippet shows a [Runner](#runner-classes) class with @BeforeClass and @AfterClass hooks which will register a set of stub services which are then available for all features that are executed by that Runner class.

```
public class APIRunner extends BaseTest {

private static WireMockServer server;

@BeforeClass
public static void startUp() {
	//starting local WireMock server and registering all stubs defined via JSON configuration
	//files and held in the src/test/resources/mocks/mappings/ folder
	
	server = WireMockHelper.startWireMock(8090, Property.getProperty("mockServicePath"));
}


@AfterClass
public static void tearDown() {
	//stopping the WireMock server
	WireMockHelper.closeWireMock(server);
}
```

[back to top](#home)

<br>

<br>

### <a name="jira-integration">19. JIRA Integration</a>

The *cf_jira_utils* module of the framework provides 3 areas of functionality:

- Export of '.feature' files based on acceptance test criteria defined in JIRA Issues (Story/Feature/Scenario)
- Update of JIRA acceptance test criteria with execution run results 
- Creation of defects/bugs for test failures and linking of these to the affected Stories/Features/Scenarios

This is not intended to replicate a full blown test management solution (such as Zephyr) but does enable projects to apply some simple (and free) traceability and tracking of requirements, tests and test results using out-of-the-box JIRA.

<br>

### <a name="json-stubs">19.1 JIRA Setup</a>

The following diagram shows the JIRA issue types that are needed, the relationships that are established when defining the User Stories and Acceptance criteria and the workflow that supports update of test status following an automated test run.

![](documentation\readme\JIRAIntegration.png)



*Example Story / Feature / Scenarios in JIRA:*

The snippets below show a sample user story with 1 linked feature and 4 linked scenarios.  The feature and 2 of the scenarios are also shown.

![](documentation\readme\JIRAIntegration1.png)

<br>

### <a name="json-stubs">19.2 Exporting Cucumber Features from JIRA</a>

The framework's bulk export utility generates feature files named using the Id and Summary fields of the JIRA Feature issue.  From the example above the feature would be name as i.e. '*AUT-91_DelegateJourneyPlanner.feature*'.

The files are saved into a folder location which is specified as a runtime parameter.  For example:

`-Dcukes.jira.featuresPath=src/test/resources/features`

The contents of the '*.feature*' file are created as shown below:

![](documentation\readme\JIRAIntegration2.png)

An extract from JIRA can be performed simply by running the *JIRAExportFeatures* class of the framework.  A simple way to do this is to set up and export test suite as follows and then run the framework as normal (see earlier sections):

```
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="feature export">
    <test name="JIRA export">
        <classes>
         	<class name="uk.ndc.csa.utilities.jira.JIRAExportFeatures"/>
        </classes>
    </test>
</suite>
```



The extract could be performed before each test run, however, the suggested approach is to perform standalone extracts and include the extracted features in the test project repository.

Note that the extract includes the JIRA priority and last run status as tags for the scenarios which also provide some additional flexibility when filtering which tests to execute, for example:

- run all high priority tests for stories <x, y, z>
- run all medium priority tests that are currently not executed or failed

### <br><a name="json-stubs">19.3 Updating JIRA with Run Results</a>

Following an execution run the framework updates the run results to JIRA by performing transition actions on the JIRA workflow for each executed Scenario and Feature issue.

If the scenario was a simple scenario then the status of the JIRA issue is set to passed if the Cucumber result for that scenario was also passed, otherwise set to failed.

If the scenario included an Examples table then the status of the JIRA issue is set to passed if the Cucumber result for each and every executed dataset was passed, otherwise set to failed.

The status of a JIRA Feature is set to passed if the status (from above) of every associated JIRA Scenario is passed, otherwise set to failed.

The test execution status is then visible against each linked feature/scenario when viewing a User Story.

In addition to updating the execution status test failure bug(s) can also be created.  There are 3 options based on the value that is set for the runtime parameter *cukes.jira.bugLevel*:

- `cukes.jira.bugLevel=0` -> no bugs raised
- `cukes.jira.bugLevel=1` -> single bug raised for the overall run
- `cukes.jira.bugLevel=2` -> individual bug raised per failed scenario

Bugs that are raised per scenario are linked to that specific scenario and its associated features and user stories.  Details of the failure reason are included in the bug description.

For a single run level bug the overall run report (aka the Extent Report) is zipped and attached to the bug, and the bug is linked to each failed scenario and its associated features and user stories.

The following snippet shows example user stories after execution:

![](documentation\readme\JIRAIntegration3.png)

<br>

To perform the results update simply requires an additional task to be added to the end of a test execution suite:

```
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="feature export">

	......
	
     <test name="JIRAUpdate">
      	<classes>
      		<class name="uk.ndc.csa.utilities.jira.JIRAUpdate"/>
      	</classes>
     </test>
</suite>
```



The framework is then executed as normal with the appropriate runtime parameters set for the JIRA integration functionality:

`jira.cukes=true`

`jira.cukes.bugLevel=<0, 1, or 2>`

[back to top](#home)

<br>

<br>

### <a name="framework-enhancement-and-collaboration">20. Framework Enhancement and Collaboration</a>

The source code for the framework is held in an Accenture [innersource repository](https://innersource.accenture.com/projects/ATAC/repos/cukes_automation/browse/cukesframework)  with a gradle project for each module.

Projects can collaborate on enhancements to the framework by forking the repository, making changes to one or more of the modules and submitting pull requests.  Pull requests will be then reviewed, approved and merged.

Once a merge has been completed a new jar (with incremented version) will be generated for each updated module.  The jar(s) will be published to [Nexus](http://34.252.181.171/nexus/#view-repositories;cukes-framework~browsestorage) ready for consumption by any project and will be supported by a release note that will be added to the [Innersource Git](https://innersource.accenture.com/projects/ATAC/repos/cukes_automation/browse/cukesframework) repo alongside the source code.

When creating and testing enhancements to the framework a test project can be easily switched from the framework jars to instead use a locally cloned/amended version of framework source code.  This requires only a minor update to the *build.gradle* and *settings.gradle* files of the test project:

- *settings.gradle* updated to define the framework modules as projects and include the relative paths from the test project
- *build.gradle* updated to switch the dependencies from nexus jars to local projects

Example settings.gradle with locally referenced *cf_common_utils* and *cf_selenium_utils* projects:
```
include ':cf_api_utils', ':cf_selenium_utils', ':cf_common_utils', ':cf_mocking_utils', ':cf_database_utils', ':cf_jira_utils'
include ':cf_selenium_utils', ':cf_common_utils'
project(':cf_common_utils').projectDir = new File(settingsDir, '../../cukes_automation/cukesframework/cf_common_utils')
project(':cf_selenium_utils').projectDir = new File(settingsDir, '../../cukes_automation/cukesframework/cf_selenium_utils')

rootProject.name = 'MyTestProject'
```

<br>

Example build.gradle showing the dependencies section with locally referenced *cf_common_utils* and *cf_selenium_utils* project source code rather than jar based dependencies:

```
dependencies { 
	compile project(':cf_common_utils')				
	compile project(':cf_selenium_utils') 
}
```

[back to top](#home)

<br><br>

### <a name="template-and-demo-projects">21. Template and Demo Projects</a>

A demo project including a set of example browser and api tests, and a template project with the required skeleton structure and configuration files can be found in the Accenture Innersource repository:

Template: [innersource.accenture.com/projects/ATAC/repos/cukes_template/browse](https://innersource.accenture.com/projects/ATAC/repos/cukes_template/browse)

Demo:   [innersource.accenture.com/projects/ATAC/repos/cukesdemo/browse/healthconference](https://innersource.accenture.com/projects/ATAC/repos/cukesdemo/browse/healthconference)


[back to top](#home)

<br><br>


