package uk.ndc.csa.utilities.selenium.steps;


import static uk.ndc.csa.utilities.selenium.screenshot.Screenshot.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import uk.ndc.csa.utilities.common.ExcelHelper;
import uk.ndc.csa.utilities.common.JsonHelper;
import uk.ndc.csa.utilities.common.Property;
import uk.ndc.csa.utilities.common.Reporter;
import uk.ndc.csa.utilities.common.ThreadContext;
import uk.ndc.csa.utilities.selenium.driver.factory.DriverFactory;
import uk.ndc.csa.utilities.selenium.pageobjects.BasePO;
import uk.ndc.csa.utilities.selenium.pageobjects.Element;

public class BaseSteps {
	
	protected Logger log = LogManager.getLogger(this.getClass().getName());
	protected BasePO po;
	
	public BaseSteps(){
	}

	public BasePO getPO() {
		log.debug("obtaining an instance of the base page objects");
		if (po == null)
			po = new BasePO();
		return po;
	}

	public WebDriver getDriver() {
		log.debug("obtaining the driver for current thread");
		return DriverFactory.getInstance().getDriver();
	}
	
	public WebDriverWait getWait() {
		log.debug("obtaining the wait for current thread");
		return DriverFactory.getInstance().getWait();
	}

	/** Returns duration for specified waits */
	@SuppressWarnings("unused")
	private int getWaitDuration(){

		final int defaultWait = 10;	
		int duration;
		try {
			duration = ThreadContext.getInstance().getEnvironmentProps().getInt("defaultWait");
			log.debug("selenium driver wait time set from environment properties");
		} catch (Exception e) {
			duration = defaultWait;
			log.debug("selenium driver wait time not available from environment properties...default applied");
		}
		return duration; 
	}

	protected void addScreenshot() {
		try {
			Reporter.addScreenCaptureFromPath("." + File.separator + "Screenshots" + File.separator 
					+ saveScreenshot(grabScreenshot(), Reporter.getScreenshotPath()).getName());
		} catch (IOException e) {
			Reporter.addStepLog("Warning: could not add screenshot");
			log.warn("could not add screenshot", e);
		}
	}
	
	public Map<String, By> getUI(String className){
		Map<String, By> map = new HashMap<String, By>();
		Class<?> cl;
		try {
			cl = Class.forName(className);
			Constructor<?> con;
			con = cl.getConstructor();
			Object obj;
			obj = con.newInstance();
			log.debug("found uimap:"+className);
			Field[] fields = cl.getFields();

			for (Field f : fields){
				String name = f.getName().toString();
				if (f.get(obj) instanceof By) {
					By by = (By) f.get(obj);
					map.put(name, by);
					log.debug("found element:"+name);
				}
			}
		} catch (Exception e) {
			log.error("unable to process uimap",e);
			return null;
		}
		return map;	
	}
	
	public void checkElement(String fieldname, Element el, String propertyList){	
		String[] properties = propertyList.split(",");
		for (String property : properties) {
			String[] state = property.split("=");
			performOperation(el, "assert", state[0].trim(), state[1].trim(), fieldname);
		}
	}
	
	public void performOperation(Element el, String action, String type, String value, String fieldName) {
		log.debug("performing selenium operation:"+fieldName+";"+type+";"+action+";"+value);
		switch(action) {
		case "sendKeys": el.clickable().sendKeys(value);break;
		case "click": el.clickable().click();break;
		case "dropdown":
			switch (type) {
			case "selectByText": el.clickable().dropdown().selectByVisibleText(value);break;
			case "selectByIndex": el.clickable().dropdown().selectByIndex(Integer.parseInt(value));break;
			case "selectByValue": el.clickable().dropdown().selectByValue(value);break;
			}break;
		case "assert": 
			switch(type) {
			case "text": sa().assertEquals(el.getText(),value);break;
			case "selectedOption": sa().assertEquals(el.dropdown().getFirstSelectedOption(),value);break;
			case "visible":	sa().assertEquals(el.element().isDisplayed(),Boolean.parseBoolean(value),"[field: "+fieldName+"; check: visible]");break;
			case "enabled": sa().assertEquals(el.element().isEnabled(),Boolean.parseBoolean(value),"[field: "+fieldName+"; check: enabled]");	break;
			case "selected": sa().assertEquals(el.element().isSelected(),Boolean.parseBoolean(value),"[field: "+fieldName+"; check: enabled]");	break;
			case "classes" : 
				String actClasses = el.getAttribute("class");
				for (String expectedClass : value.split(" ")) {
					sa().assertTrue(includesClass(actClasses, expectedClass.trim()), "Element class not found: "+expectedClass.trim());
				}
				break;
			default: sa().assertEquals(el.getAttribute(type),value);break;
			}break;
		case "wait": 
			switch(type) {
			case "page": getPO().waitPageToLoad();break;
			case "visible": getWait().until(ExpectedConditions.visibilityOf(el.element()));break;
			case "clickable": getWait().until(ExpectedConditions.elementToBeClickable(el.element()));break;
			case "text": getWait().until(ExpectedConditions.textToBePresentInElement(el.element(),value));break;
			}break;
		}
	}

	public Boolean includesClass(String actClasses, String expClass) {
		Optional<String> classFindResult = Arrays.stream(actClasses.split(" ")).filter(cl -> cl.equals(expClass)).findFirst();
		if(classFindResult.isPresent()){
		    return true;
		}else {
			return false;
		}
	}

	public Element find(String method, String locator) {
		By by = null;
		switch (method) {
		case "id": by=By.id(locator); break;
		case "name": by=By.name(locator); break;
		case "linkText": by=By.linkText(locator); break;
		case "partialLinkText": by=By.partialLinkText(locator); break;
		case "className": by=By.className(locator); break;
		case "tagName": by=By.tagName(locator); break;
		case "css": by=By.cssSelector(locator); break;
		case "xpath": by=By.xpath(locator); break;
		}
		return getPO().findElement(by);
	}
	
	public SoftAssert sa() {
		return ThreadContext.getInstance().sa();
	}
	
	public void autoFillCheckJSON(String methodJSON, String methodName, Map<String, String> featureData) {
		String path = ThreadContext.getInstance().getPathsProps().getString("pageObjectsPath");
		JSONArray methods = null;
		try {
			methods = JsonHelper.getJSONArray(System.getProperty("user.dir")+path+methodJSON+".json",methodName);

			JSONObject pageobject = null;
			String lastPO = null;

			for (int i = 0; i< methods.length(); i++) {
				JSONObject method = methods.getJSONObject(i);
				String object = method.has("object")?method.getString("object"):null;
				String field = method.has("field")?method.getString("field"):null;
				String action = method.has("action")?method.getString("action"):null;
				String type = method.has("type")?method.getString("type"):null;
				String value = method.has("value")?method.getString("value"):null;

				if (!object.equalsIgnoreCase(lastPO)) {
					pageobject = JsonHelper.getJSONData(System.getProperty("user.dir")+path+object+".json");		
				}

				if (featureData != null  && value != null) {
					if (value.startsWith("<") && value.endsWith(">")) {
						value = featureData.get(value.replace("<","").replace(">",""));
					}
				}

				JSONObject element = pageobject.getJSONObject(field);
				Element el = find(element.getString("method"),element.getString("locator"));

				performOperation(el, action, type, value, field);

			}
			sa().assertAll();

		} catch (RuntimeException e) {
			log.error("Page Methods JSON file not found",e);
			Assert.fail("Page Methods JSON not found: "+e.getMessage());
		} 
	}

	public void autoFillCheckExcel(String methodExcel, String methodName, Map<String, String> featureData) {
		String path = ThreadContext.getInstance().getPathsProps().getString("pageObjectsPath");
		ArrayList<ArrayList<Object>> methods = null;
		try {
			methods = ExcelHelper.getDataAsArrayList(System.getProperty("user.dir")+path+methodExcel+".xlsx","methods",methodName);

			ArrayList<ArrayList<Object>> pageobject = null;
			Map<String, List<String>> pageobjectMap = new HashMap<String, List<String>>();
			String lastPO = null;

			for (int i = 0; i< methods.size(); i++) {
				ArrayList<Object> method = methods.get(i);
				String object = method.get(1)==null?null:method.get(1).toString();
				String field = method.get(2)==null?null:method.get(2).toString();
				String action = method.get(3)==null?null:method.get(3).toString();
				String type = method.get(4)==null?null:method.get(4).toString();
				String value = method.get(5)==null?null:method.get(5).toString();
				if (!object.equalsIgnoreCase(lastPO)) {
					try {
						pageobject = ExcelHelper.getDataAsArrayList(System.getProperty("user.dir")+path+methodExcel+".xlsx",object);
						for (int j=0; j< pageobject.size(); j++) {
							List<String> list = new ArrayList<String>();
							list.add(pageobject.get(j).get(1).toString());
							list.add(pageobject.get(j).get(2).toString());
							pageobjectMap.put(pageobject.get(j).get(0).toString(), list);
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}		
				}

				if (featureData != null  && value != null) {
					if (value.startsWith("<") && value.endsWith(">")) {
						value = featureData.get(value.replace("<","").replace(">",""));
					}
				}

				List<String> element = pageobjectMap.get(field);
				Element el = find(element.get(0),element.get(1));

				performOperation(el, action, type, value, field);

			}
			sa().assertAll();

		} catch (IOException e) {
			log.error("Page Methods Excel file not found",e);
			Assert.fail("Page Methods Excel not found: "+e.getMessage());
		}
	}

	

}
