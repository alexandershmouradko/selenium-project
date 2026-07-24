# RELEASE NOTES

### Framework version 2.0.3 - 29/11/2018

- cf-api-utils: none
- cf-common-utils: none
- cf-database-utils: none
- cf-jira-utils:  nonegit 
- cf-mocking-utils: none
- cf-selenium-utils: fix applied to Element class to remove ExpectedConditions.refreshed() from some utility methods to resolve unexpected test failures with selenium hanging while waiting for element refresh.

### Framework version 2.0.2 - 28/11/2018

- cf-api-utils: none

- cf-common-utils: none

- cf-database-utils: none

- cf-jira-utils:  none

- cf-mocking-utils: none

- cf-selenium-utils: fix applied to Element class to remove calls to Clickable() method from other utility methods such as sendKeys(), ckick().  This avoids failures where selenium is unable to determine whether an element is clickable.  

  Test code can optionally apply a clickable check using the following pattern:  findElement(By("foo")).clickable().click(); or findElement(By("foo")).clickable().sendKeys().  The clickable() method returns the Element object and can therefore be chained anywhere in a statement.

### Framework version 2.0.1 - 27/11/2018

- cf-api-utils: none
- cf-common-utils: none
- cf-database-utils: none
- cf-jira-utils:  none
- cf-mocking-utils: none
- cf-selenium-utils: fix applied to Capabilities class to allow JOB_NAME and BUILD_NUMBER runtime parameters to be optional

### Framework version 2.0.0 - 09/11/2018

- cf-api-utils: none
- cf-common-utils: none
- cf-database-utils: none
- cf-jira-utils:  none
- cf-mocking-utils: none
- cf-selenium-utils: 
  - BasePO class amended to add methods  getDriver() and getWait() to return the objects for the running thread from the driver factory.  The local driver and wait variables and instantiation of these variables in the constructor have been removed. 
  - BaseSteps class amended to add methods  getDriver() and getWait() to return the objects for the running thread.  The local driver and wait variables and instantiation of these variables in the constructor have been removed. 
  - Above changes rippled through utility methods in BasePO, BaseSteps, CommonSteps and Wireframe classes to reference the new methods wherever driver or wait objects are used rather than the local variables.
  - The above changes were needed to avoid duplicate/erroneous object creation which had a downstream impact on the test naming that was applied when running against Saucelabs.
  - Capabilities class minor change also applied to default the job name if runtime param not specified



**NOTE: any test projects that referenced the 'driver' or 'wait' objects directly in the test code will now need to replace these with the new methods 'getDriver()' and 'getWait()'.**  

For example:

​	driver.getTitle()	becomes	getDriver().getTitle();

​	wait.until(....); 	becomes 	getWait().until(.......);

The changes can be quickly applied by using the global replace tools within any IDE.

### Framework version 1.4.0 - 09/11/2018

- general:
  - build.gradle file updated to add Atlassian repository
- cf-api-utils:
  - amended RestAssuredHelper.setBody() and RestAssuredSteps.requestBody() methods to remove the defaulting of the Content-Type (NOTE: content type should be specified in the feature/scenario as a header parameter where needed by the api being invoked)
  - amended the hasSize / isEmpty matchers to work on json objects as well as arrays
- cf-common-utils: none
- cf-database-utils: none
- cf-jira-utils:  none
- cf-mocking-utils: none
- cf-selenium-utils: 
  - Bug fix  to DriverFactory case statement to correct typo and use "htmlunit" as browser name
  - Bug fix to ChromeDriverManager, EdgeDriverManager, FirefoxDriverManager, IEDriverManager, PhantomJSDriverManager to avoid null pointer exception and handle error when test project doesn't specify the optional 'cukes.webdrivermanager' runtime parameter
  - Minor change to BasePO.findClickable() method to use clickable() wait method
  - Minor change to BasePO.findElements(By by, By sub, int...delay) method to change wait clause from 'visibilityOfNestedElementsLocatedBy' to 'presenceOfNestedElementsLocatedBy'
  - Element.refind() method amended to perform multiple retries where exceptions occur (e.g. stale element).  Usage of this method has been rippled through all other methods that perform element operations (e.g. getText(), sendKeys(), getAttribute() etc)

### Framework version 1.3.0 - 26/09/2018

- cf-api-utils:
  - added regular expression checking for API response body
  - added additional validation check options (equals, regex, null) to api header fields
  - added RequestSpecification object to the RestContext/RestData classes to allow the in-built framework steps to be extended with custom steps that can access the same restassured objects

- cf-common-utils: none
- cf-database-utils: none
- cf-jira-utils:  none
- cf-mocking-utils: none

- cf-selenium-utils: 
  - Amended SauceLabsDriverManager to include file locator for performing file uploads
  - Bug fix to BasePO class method 'exists()' to ensure correct result returned
  - Minor change to Element class method 'click()' so that it attempts native click before javascript click
  - various log4j logging statements added to base selenium classes

### Framework version 1.2.0 - 25/07/2018

* general:

  - build.gradle file updated to enabling snapshot repo and snapshot jars for daily builds
  - added skyscreamer and pdfbox to the framework dependencies

- cf-api-utils:
  - use skyscreamer library to validate whole json objects allowing relaxed and strict field ordering and existence checks
  - enable JSON body validation to be performed either against a json response received from RestAssured or any specified JSON message/string

- cf-common-utils:

  - add new helper class for PDF validation

- cf-database-utils: none

- cf-jira-utils: 

  - Feature names based on feature issue summary rather than story summary

  - Story and Feature issue id tags included in feature

  - Story description can now be optionally included in the exported feature using runtime option 

    -Dcukes.jira.useStoryDesc=true|false

- cf-mocking-utils: none

- cf-selenium-utils: none


### Framework version 1.1.0 - 06/07/2018
* general:

  - adding Log4J dependencies to the framework
  - add various logging statements throughout framework
  - resolved clashing transitive dependencies of cf_jira_utils and cf_database_utils (elastic search)

- cf-api-utils:
  - changes applied to allow API structures to be defined more granularly when using the "And the response body contains" gherkin step (based on request path and method)

- cf-common-utils: none
- cf-database-utils: none
- cf-jira-utils: none
- cf-mocking-utils: none

- cf-selenium-utils: none

### Framework version 1.0.0 - 04/07/2018
* Initial Baseline for cucumber-framework and its sub-modules: cf-api-utils, cf-common-utils, cf-database-utils, cf-jira-utils, cf-mocking-utils, cf-selenium-utils
