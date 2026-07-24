package uk.ndc.csa.utilities.selenium.pageobjects;

import static uk.ndc.csa.utilities.common.JsonHelper.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;

import uk.ndc.csa.utilities.common.ThreadContext;

/** 
 * Validates a given page properties based on supplied UIMap class and group.
 * Within the UIMap class any page elements to be checked should be annotated
 * based on the UIAnnot interface.
 * 
 * Each element may have multiple annotation groups (up to 5) and each annotation group
 * contains an array list of properties to be checked.
 * 
 * Currently the checks performed are visibility, enablement, and any given attribute value
 */
@Deprecated
public class WireFrame extends BasePO{

	public void checkState(String fieldname, Element el, String propertyList){	
		String[] properties = propertyList.split(",");
		for (String property : properties) {
			String[] state = property.split("=");
			checkState(fieldname, el, state[0].trim(), state[1].trim());
		}
	}
	
	public void checkState(String fieldname, Element el, String property, String state){		
		switch(property){
		case "visible":
			System.out.println(el.element().isDisplayed());
			sa().assertEquals(el.element().isDisplayed(),Boolean.parseBoolean(state),"[field: "+fieldname+"; check: visible]");break;
		case "enabled":
			System.out.println(el.element().isEnabled());
			sa().assertEquals(el.element().isEnabled(),Boolean.parseBoolean(state),"[field: "+fieldname+"; check: enabled]");	break;
		case "selected":
			sa().assertEquals(el.element().isSelected(),Boolean.parseBoolean(state),"[field: "+fieldname+"; check: enabled]");	break;
		default:
			sa().assertEquals(el.getAttribute(property),state,"[field: "+fieldname+"; attribute: "+property+", check: value]");break;
		}
	}

	public void checkValue(String fieldname, Element el, String type, String value){
		if (!value.equalsIgnoreCase("<none>")){		
			if (type.equalsIgnoreCase("checkbox")){
				ThreadContext.getInstance().sa().assertEquals(el.element().isSelected(),Boolean.parseBoolean(value),"[field: "+fieldname+"; check: value]");
			}else if(type.equalsIgnoreCase("dropdown")){
				ThreadContext.getInstance().sa().assertEquals(el.dropdown().getFirstSelectedOption().getText(),value,"[field: "+fieldname+"; check: dropdown value]");
			}else if(type.equalsIgnoreCase("input")){
				ThreadContext.getInstance().sa().assertEquals(el.element().getAttribute("value"),value,"[field: "+fieldname+"; check: innertext]");
			}else if(type.equalsIgnoreCase("value")){
				ThreadContext.getInstance().sa().assertEquals(el.element().getAttribute("value"),value,"[field: "+fieldname+"; check: value]");
			}else if(type.equalsIgnoreCase("text")){
				ThreadContext.getInstance().sa().assertEquals(el.element().getText(),value,"[field: "+fieldname+"; check: innertext]");
			}else {
				ThreadContext.getInstance().sa().assertEquals(el.element().getAttribute(type),value,"[field: "+fieldname+"; attribute: "+type+"; check: value]");
			}
		}
	}

	public void performAction(Element el, String type, String value){
		if (!value.equalsIgnoreCase("<none>")){	
			if (type.equalsIgnoreCase("checkbox")){
				Boolean state = el.clickable().element().isSelected();
				if (state != Boolean.parseBoolean(value)){
					el.click();
				}
			}else if(type.equalsIgnoreCase("button") 
					|| type.equalsIgnoreCase("radio") 
					|| type.equalsIgnoreCase("link")){
				el.clickable().click();
			}else if(type.equalsIgnoreCase("dropdown")){
				el.clickable().dropdown().selectByVisibleText(value);
			}else if(type.equalsIgnoreCase("input")){
				el.clickable().clear().sendKeys(value);
			}
		}
	}


	public void checkValue(JSONObject json, List<Map<String,String>> maps) {				
		for (int i=0;i<maps.size();i++) {
			String field = maps.get(i).get("field");
			String value = maps.get(i).get("value");
			String type = maps.get(i).get("type");					//String type = json.getJSONObject(field).getString("type");
			String method = json.getJSONObject(field).getString("method");
			String locator = json.getJSONObject(field).getString("locator");
			Element el = find(method, locator);
			if (type.equalsIgnoreCase("state")) {
				for (String s : value.split(",")){
					String property = s.split("=")[0];
					String state = s.split("=")[1];
					checkState(field, el, property, state);	
				}
			}else if (type.equalsIgnoreCase("attribute")) {
				String att = value.split("=")[0];
				String val = value.split("=")[1];
				getWait().until(ExpectedConditions.visibilityOf(el.element()));
				checkValue(field, el, att, val);	
			}else {
				getWait().until(ExpectedConditions.visibilityOf(el.element()));
				checkValue(field, el, type, value);	
			}
		}
		ThreadContext.getInstance().sa().assertAll();
	}

	public void performAction(JSONObject json, List<Map<String,String>> maps) {		
		for (int i=0;i<maps.size();i++) {
			String field = maps.get(i).get("field");
			String value = maps.get(i).get("value");
			String type = maps.get(i).get("type");					//String type = json.getJSONObject(field).getString("type");
			String method = json.getJSONObject(field).getString("method");
			String locator = json.getJSONObject(field).getString("locator");
			Element el = find(method, locator);
			performAction(el, type, value);
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
		return findElement(by);
	}


	@Deprecated
	public void checkElementValue(String ui, List<Map<String,String>> maps) {				
		try{
			Class<?> c = Class.forName(ui);
			Object obj = c.newInstance();
			int[] arg = {};
			for (int i=0;i<maps.size();i++) {
				String field = maps.get(i).get("field");
				Method method = obj.getClass().getDeclaredMethod(field,int[].class);
				String value = maps.get(i).get("value");
				String type = method.getAnnotation(UIAnnot.class).type();
				Element el = (Element) method.invoke(obj,arg);
				getWait().until(ExpectedConditions.visibilityOf(el.element()));
				checkValue(field, el, type, value);
			}
			ThreadContext.getInstance().sa().assertAll();
		} catch (Exception e) {
			Assert.fail("Could not perform validation: "+e.getMessage());
		} 
	}

	@Deprecated
	public void checkElementValue(String ui, JSONObject json) {				
		try{
			Class<?> c = Class.forName(ui);
			Object obj = c.newInstance();
			int[] arg = {};
			String[] fields = JSONObject.getNames(json);
			if (fields !=null){
				for (String field: fields) {
					Method method = obj.getClass().getDeclaredMethod(field,int[].class);
					String value = json.getJSONObject(field).getString("value");
					String type = method.getAnnotation(UIAnnot.class).type();
					Element el = (Element) method.invoke(obj,arg);
					getWait().until(ExpectedConditions.visibilityOf(el.element()));
					checkValue(field, el, type, value);
				}
			}
		} catch (Exception e) {
			Assert.fail("Could not perform validation: "+e.getMessage());
		} 
	}

	@Deprecated
	public void checkByValue(String ui, List<Map<String,String>> maps) {				
		Map<String, UIMap> uimap = getUIMap(ui);
		for (int i=0;i<maps.size();i++) {
			String field = maps.get(i).get("field");
			String value = maps.get(i).get("value");
			String type = uimap.get(field).getType();
			By by = uimap.get(field).getBy();	
			getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
			Element el = findElement(by);
			checkValue(field, el, type, value);
		}
		ThreadContext.getInstance().sa().assertAll();
	}

	@Deprecated
	public void checkByValue(String ui, JSONObject json) {		
		Map<String, UIMap> uimap = getUIMap(ui);
		String[] fields = JSONObject.getNames(json);

		if (fields !=null){
			for (String field: fields) {
				String value = json.getJSONObject(field).getString("value");
				String type = uimap.get(field).getType();
				By by = uimap.get(field).getBy();
				getWait().until(ExpectedConditions.visibilityOfElementLocated(by));
				Element el = findElement(by);
				checkValue(field, el, type, value);
			}
			ThreadContext.getInstance().sa().assertAll();
		}	

	}

	@Deprecated
	public void updateElementValue(String ui, List<Map<String,String>> maps) {	
		try{
			Class<?> c = Class.forName(ui);
			Object obj = c.newInstance();
			int[] arg = {};
			for (int i=0;i<maps.size();i++) {
				String field = maps.get(i).get("field");
				Method method = obj.getClass().getDeclaredMethod(field,int[].class);
				String value = maps.get(i).get("value");
				String type = method.getAnnotation(UIAnnot.class).type();
				Element el = (Element) method.invoke(obj,arg);
				performAction(el, type, value);
			}
		} catch (Exception e) {
			Assert.fail("Could not perform update: "+e.getMessage());
		} 
	}

	@Deprecated
	public void updateElementValue(String ui, String jsonFile, String dataset) {		
		try{
			Class<?> c = Class.forName(ui);
			Object obj = c.newInstance();
			int[] arg = {};
			JSONObject json = getJSONData(jsonFile, dataset);
			String[] fields = JSONObject.getNames(json);

			if (fields !=null){
				for (String field: fields) {
					Method method = obj.getClass().getDeclaredMethod(field,int[].class);
					String value = json.getJSONObject(field).getString("value");
					String type = method.getAnnotation(UIAnnot.class).type();
					Element el = (Element) method.invoke(obj,arg);
					performAction(el, type, value);
				}
			}
		} catch (Exception e) {
			Assert.fail("Could not perform update: "+e.getMessage());
		} 
	}

	@Deprecated
	public void updateByValue(String ui, List<Map<String,String>> maps) {		
		Map<String, UIMap> uimap = getUIMap(ui);
		for (int i=0;i<maps.size();i++) {
			String field = maps.get(i).get("field");
			String value = maps.get(i).get("value");
			String type = uimap.get(field).getType();
			By by = uimap.get(field).getBy();
			Element el = findElement(by);
			performAction(el, type, value);
		}
	}

	@Deprecated
	public void updateByValue(String ui, String jsonFile, String dataset) {		
		Map<String, UIMap> uimap = getUIMap(ui);
		JSONObject obj = getJSONData(jsonFile, dataset);
		String[] fields = JSONObject.getNames(obj);

		if (fields !=null){
			for (String field: fields) {
				String value = obj.getJSONObject(field).getString("value");
				String type = uimap.get(field).getType();
				By by = uimap.get(field).getBy();
				Element el = findElement(by);
				performAction(el, type, value);
			}
		}
	}

	@Deprecated
	public void checkElementProperties(String uiMapPath, String group)  {	
		try {
			Class<?> c = Class.forName(uiMapPath);
			Object obj =  c.newInstance();
			Method[] methods = c.getMethods();
			int[] arg = {};
			for (Method method : methods){
				String name = method.getName().toString();
				UIAnnot annot = method.getAnnotation(UIAnnot.class);
				String[] annotlist = null;
				if (annot != null){
					switch (group){
					case "group1": annotlist = annot.group1();break;
					case "group2": annotlist = annot.group2();break;
					case "group3": annotlist = annot.group3();break;
					case "group4": annotlist = annot.group4();break;
					case "group5": annotlist = annot.group4();break;
					}
					if (annotlist.length>0){
						Element el = (Element) method.invoke(obj,arg);

						for (String s : annotlist){
							String property = s.split("=")[0];
							String state = s.split("=")[1];
							checkState(name, el, property, state);	
						}
					}
				}
			}	

			ThreadContext.getInstance().sa().assertAll();
		} catch (Exception e) {
			Assert.fail("Could not perform validation: "+e.getMessage());
		} 
	}

	@Deprecated
	public void checkByProperties(String uiMapPath, String group)  {	
		try {
			Class<?> c = Class.forName(uiMapPath);

			Object obj =  c.newInstance();
			Field[] fields = c.getFields();
			for (Field f : fields){
				String name = f.getName().toString();
				By by = (By) f.get(obj);
				UIAnnot annot = f.getAnnotation(UIAnnot.class);
				String[] annotlist = null;
				if (annot != null){
					switch (group){
					case "group1": annotlist = annot.group1();break;
					case "group2": annotlist = annot.group2();break;
					case "group3": annotlist = annot.group3();break;
					case "group4": annotlist = annot.group4();break;
					case "group5": annotlist = annot.group4();break;
					}
					if (annotlist.length>0){
						Element el = findElement(by);
						for (String s : annotlist){
							String property = s.split("=")[0];
							String state = s.split("=")[1];
							checkState(name, el, property, state);	
						}
					}
				}
			}	

			ThreadContext.getInstance().sa().assertAll();
		} catch (Exception e) {
			Assert.fail("Could not perform validation: "+e.getMessage());
		} 
	}

	@Deprecated
	public Map<String, UIMap> getUIMap(String className) {
		Map<String, UIMap> map = new HashMap<String, UIMap>();
		Class<?> cl;
		try {
			cl = Class.forName(className);

			//Constructor<?> con = cl.getConstructor(WebDriver.class, WebDriverWait.class);
			//Object obj = con.newInstance(driver, wait);
			Constructor<?> con = cl.getConstructor();
			Object obj = con.newInstance();
			Field[] fields = cl.getFields();

			for (Field f : fields){
				UIMap uimap = new UIMap();
				String name = f.getName().toString();
				String annot = f.getAnnotation(UIAnnot.class).type();
				By by = (By) f.get(obj);
				uimap.setBy(by);
				uimap.setType(annot);
				map.put(name, uimap);
			}
			return map;	
		} catch (Exception e) {
			return null;
		} 

	}
}
