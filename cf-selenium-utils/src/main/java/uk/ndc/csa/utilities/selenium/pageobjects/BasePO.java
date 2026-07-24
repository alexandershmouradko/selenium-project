package uk.ndc.csa.utilities.selenium.pageobjects;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.asserts.SoftAssert;

import uk.ndc.csa.utilities.common.ThreadContext;
import uk.ndc.csa.utilities.selenium.driver.factory.DriverFactory;

/**
 * Utility class providing set of selenium wrapper methods for finding web elements with build in waits
 */

public class BasePO {
	protected Logger log = LogManager.getLogger(this.getClass().getName());
	
	public BasePO() {
	}
	
	public WebDriver getDriver() {
		log.debug("obtaining the driver object for current thread");
		return DriverFactory.getInstance().getDriver();
	}
	
	public WebDriverWait getWait() {
		log.debug("obtaining the wait object for current thread");
		return DriverFactory.getInstance().getWait();
	}
	
	public void initialise(Object obj){
		PageFactory.initElements(getDriver(), obj);
	}

	/*
	 * Set of common methods for Page Objects which are defined with either
	 * standard By locators or PageFactory
	 */

	public BasePO gotoURL(String url){
		log.debug("navigating to url:"+url);
		getDriver().get(url);
		return this;
	}

	/** Wait for page to load*/
	public BasePO waitPageToLoad() {
		domLoaded();
		jqueryLoaded();
		//angularLoaded();
		return this;
    }

	/** Wait for page to load based on document.readyState=complete*/
	public void domLoaded() {
		log.debug("checking that the DOM is loaded");
		final JavascriptExecutor js = (JavascriptExecutor)getDriver();
		Boolean domReady = js.executeScript("return document.readyState").equals("complete");

		if (!domReady){
			getWait().until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
					return (js.executeScript("return document.readyState").equals("complete"));}
			});
		}
	}

	/** Wait for JQuery to load */
	private void jqueryLoaded() {
		log.debug("checking that any JQuery operations complete");
		final JavascriptExecutor js = (JavascriptExecutor)getDriver();

		if ((Boolean) js.executeScript("return typeof jQuery != 'undefined'")){
			boolean jqueryReady = (Boolean) js.executeScript("return jQuery.active==0");

			if (!jqueryReady){
				getWait().until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver d) {
						return (Boolean) js.executeScript("return window.jQuery.active === 0");
					}
				});
			}
		}
	}

	 /** Wait for AngularJs to load */
	 public void angularLoaded() {
		 log.debug("checking that any AngularJS operations complete");
		 final JavascriptExecutor js = (JavascriptExecutor)getDriver();
		 if ((Boolean) js.executeScript("if (window.angular){return true;}")){
			Boolean angularInjectorUndefined = (Boolean) js.executeScript("return angular.element(document).find('body').injector() === undefined");

			 if (!angularInjectorUndefined){
				Boolean angularReady = (Boolean) js.executeScript("return angular.element(document).find('body').injector().get('$http').pendingRequests.length === 0");

				 if (!angularReady){
					 getWait().until(new ExpectedCondition<Boolean>() {
						 public Boolean apply(WebDriver d) {
							 return js.executeScript("return angular.element(document).find('body').injector().get('$http').pendingRequests.length === 0").equals(true);
						 }
					 });
				 }
			 }else{
				 log.debug("no AngularJS injector defined so cannot wait");
			 }
		 }
	 }


	 /*
	 * Further methods for Page Objects when using Standard By Locators (non PageFactory)
	 * These utilise an additional Element class that wraps WebElement to provide additional
	 * functionality or composite functions (see Element class for details).
	 * These methods include in-built waits when finding elements as needed.
	 */

	/** Returns first element occurrence matching the supplied locator if an element exists in DOM*/
	public Element findElement(By by, int...delay) {
		Element el = new Element(by, delay);
		return scroll(el);
    }

	/** Returns first element occurrence matching the supplied locator if an element is clickable*/
	public Element findClickable(By by, int...delay) {
		Element el = new Element(by, delay).clickable();
		return scroll(el);
    }

	/** Returns first element occurrence matching the supplied locator if an element exists in DOM*/
	public Element findElement(ExpectedCondition<?> exp, int...delay) {
		Element el = new Element(exp, delay);
		return scroll(el);
    }

	/** Finds first element within current element matching the supplied locator */
	public Element findElement(By by, By sub, int...delay){
		Element el = new Element(ExpectedConditions.presenceOfNestedElementLocatedBy(by, sub), delay);
		return scroll(el);
	}

	/** Returns all element occurrences matching the supplied locator if the elements exist in DOM*/
	public List<Element> findElements(By by, int...delay) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(delay.length > 0 ? delay[0] : getWaitDuration()));
		List<WebElement> els = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
		List<Element> elements = setElements(els);

		scroll(elements.get(0));
		return elements;
    }

	/** Finds all elements within current element matching the supplied locator */
	public List<Element> findElements(By by, By sub, int...delay){
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(delay.length > 0 ? delay[0] : getWaitDuration()));
		List<WebElement> els = wait.until(ExpectedConditions.presenceOfNestedElementsLocatedBy(by, sub));
		List<Element> elements = setElements(els);
		scroll(elements.get(0));
		return elements;
	}

	/** Returns all element occurrences matching the supplied locator if the elements exist in DOM*/
	public List<Element> findElements(ExpectedCondition<List<WebElement>> exp, int...delay) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(delay.length > 0 ? delay[0] : getWaitDuration()));
		List<WebElement> els = wait.until(exp);
		List<Element> elements = setElements(els);
		scroll(elements.get(0));
		return elements;
    }

	/** Scrolls to element to avoid issues with element location being unclickable */
	public Element scroll(Element el){
		if (ThreadContext.getInstance().getSeleniumProps()
				.getBoolean("scrollToElements."+ThreadContext.getInstance().getBrowserName().replaceAll("\\s", ""),false) &&
			el.element() != null){
			scrollTo(el);
		}
		return el;
	}

	/** Scrolls to element to avoid issues with element location being unclickable */
	public Element scrollTo(Element el){
		try {
			((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", el.element());
		}catch (Exception e){
			log.warn("scrolling to elemennt failed",e);
		}
		return el;
	}

	/** Builds and returns list of nested elements  */
	public List<Element> setElements(List<WebElement> els){
		List<Element> list = new ArrayList<Element>();
		for (WebElement el : els){
			list.add(new Element(el));
		}
		return list;
	}

	/** Checks for element existence */
	public boolean exist(By by, int...delay){
		Element el = new Element(by, delay);
		Boolean exists = (el.element() == null)?false:true;
		return exists;
	}

	 /** Returns duration for specified waits */
		public int getWaitDuration(){
			final int defaultWait = 10;
			int duration;
			try {
				duration = ThreadContext.getInstance().getEnvironmentProps().getInt("defaultWait");
				log.debug("selenium getDriver() getWait() time set from environment properties");
			} catch (Exception e) {
				duration = defaultWait;
				log.debug("selenium getDriver() getWait() time not available from environment properties...default applied");
			}
			return duration;
		}

		public void switchWindow(String parent) {
			log.debug("parent window handle:"+parent);
			switching: while (true){
				for (String handle : getDriver().getWindowHandles()){
					if (!handle.equals(parent)){
						log.debug("switching to window handle:"+handle);
						getDriver().switchTo().window(handle);
						break switching;
					}
				}
			}
		}
		
		public void switchFrame(String frameLocator) {	
			getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameLocator));
		}
		
		public void switchFrame(By by) {	
			getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(by));
		}
		
		public void switchFrame(Element el) {	
			getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(el.element()));
		}
		
		public void switchToDefaultContent() {	
			getDriver().switchTo().defaultContent();
		}
		
		public SoftAssert sa() {
			return ThreadContext.getInstance().sa();
		}
}
