# RELEASE NOTES

### Framework version 0.0.3 - 17/10/2017
* Introduction of /config/paths.properties file to hold configuration project paths.  Default paths are defined but can be overridden:
  *  environmentsPath=./src/test/resources/config/environments/
  *  seleniumRuntimePath=./src/test/resources/config/selenium/
  *  seleniumStackPath=./src/test/resources/config/selenium/stacks/
  *  mockServicePath=./src/test/resources/mocks/
  *  apiStructurePath=./src/test/resources/apistructures/
  *  testDataPath=./src/test/resources/testdata/
  *  reportPath=./RunReports/
* Use of paths.properties rippled through the framework with changes to all modules  
* Use of SLF4J for logging added to ThreadContext within cf_common_utils


Current framework jar versions:  
  cf_common_utils-0.0.2&nbsp;&nbsp;&nbsp;&nbsp;cf_api_utils-0.0.2&nbsp;&nbsp;&nbsp;&nbsp;cf_database_utils-0.0.2&nbsp;&nbsp;&nbsp;&nbsp;cf_mocking_utils-0.0.2&nbsp;&nbsp;&nbsp;&nbsp;cf_selenium_utils-0.0.3


### Framework version 0.0.2 - 10/10/2017
* All methods of the *Element* class within cf_selenium_utils updated to take WebElement object as input param rather than By object.
* For example calls to the method Element.clickJS should now be made as `clickJS(findElement(By);`  



Current framework jar versions:  
  cf_common_utils-0.0.1&nbsp;&nbsp;&nbsp;&nbsp;cf_api_utils-0.0.1&nbsp;&nbsp;&nbsp;&nbsp;cf_database_utils-0.0.1&nbsp;&nbsp;&nbsp;&nbsp;cf_mocking_utils-0.0.1&nbsp;&nbsp;&nbsp;&nbsp;cf_selenium_utils-0.0.2

### Framework version 0.0.1 - 10/10/2017
* Initial working version of the test framework
* Jars uploaded to central nexus http://34.252.181.171/nexus/content/repositories/cukes-framework/
  * cf_common_utils-0.0.1
  * cf_api_utils-0.0.1
  * cf_database_utils-0.0.1
  * cf_mocking_utils-0.0.1
  * cf_selenium_utils-0.0.1
