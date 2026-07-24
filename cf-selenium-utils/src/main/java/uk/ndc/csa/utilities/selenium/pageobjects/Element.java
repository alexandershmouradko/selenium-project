package uk.ndc.csa.utilities.selenium.pageobjects;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import uk.ndc.csa.utilities.common.ThreadContext;
import uk.ndc.csa.utilities.selenium.driver.factory.DriverFactory;

/**
 * Utility class providing set of selenium wrapper methods
 */






public class Element {
    private By by;
    private WebElement element;
    private WebDriverWait wait;
    protected Logger log = LogManager.getLogger(this.getClass().getName());

    public Element(WebElement e) {
        wait = new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(getWaitDuration()));
        this.element = e;
    }

    public Element(WebElement e, By by) {
        wait = new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(getWaitDuration()));
        this.element = e;
        this.by = by;
    }

    public Element(By by, int... delay) {
        this.by = by;
        try {
            wait = new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(delay.length > 0 ? delay[0] : getWaitDuration()));
            this.element = wait.until(ExpectedConditions.presenceOfElementLocated(by));
            log.debug("element located successfully:" + by.toString());
        } catch (Exception e) {
            this.element = null;
            log.debug("element not located:" + by.toString());
            log.debug(e.getMessage());
        }
    }

    public Element(ExpectedCondition<?> exp, int... delay) {
        this.by = null;
        try {
            wait = new WebDriverWait(DriverFactory.getInstance().getDriver(), Duration.ofSeconds(delay.length > 0 ? delay[0] : getWaitDuration()));
            this.element = (WebElement) wait.until(exp);
        } catch (Exception e) {
            this.element = null;
            log.debug("element not located:" + by.toString());
            log.debug(e.getMessage());
        }
    }


    public By by() {
        return by;
    }

    public WebElement element() {
        return element;
    }

    /**
     * searches again for the element using the by
     */
    public Element refind(int... retries) {
        log.info("Attempting to refind the element: " + by.toString());
        int attempts = 0;
        Boolean retry = true;
        int maxRetry = retries.length > 0 ? retries[0] : getFindRetries();
        while (attempts < maxRetry && retry) {
            try {
                log.debug("retry number " + attempts);
                this.element = wait.until(ExpectedConditions.presenceOfElementLocated(by));
                this.element.getTagName();
                retry = false;
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    log.debug(e1.getMessage());
                }
            }
            attempts++;
        }
        return this;
    }

    /**
     * Returns a nested element
     */
    public Element findElement(By by) {
        return new Element((WebElement) wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(this.element, by)), by);
    }

    /**
     * Returns list of nested elements
     */
    public List<Element> findElements(By by) {
        List<WebElement> els = (List<WebElement>) wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(this.element, by));
        List<Element> list = new ArrayList<Element>();
        for (WebElement el : els) {
            list.add(new Element(el));
        }
        return list;
    }

    /**
     * wait for the element to become visible
     */
    public Element visible(int... retries) {
        this.element = wait.until(ExpectedConditions.visibilityOf(this.element));
        return this;
    }

    /**
     * wait for the element to become clickable
     */
    public Element clickable(int... retries) {
        try {
            this.element = wait.until(ExpectedConditions.elementToBeClickable(this.element));
        }catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                return this.clickable(0);
            } else {
                throw e;
            }
        }
        return this;
    }

    public String getText(int... retries) {
        String str = null;
        try {
            str = this.element.getText();
        } catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                return this.getText(0);
            } else {
                throw e;
            }
        }
        return str;
    }

    public String getValue(int... retries) {
        String str = null;
        try {
            str = this.element.getAttribute("value");
        } catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                return this.getValue(0);
            } else {
                throw e;
            }
        }
        return str;
    }

    public String getAttribute(String attr, int... retries) {
        try {
            return this.element.getAttribute(attr);
        } catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                return this.getAttribute(attr, 0);
            } else {
                throw e;
            }
        }
    }

    public Element clear(int... retries) {
        try {
            this.element().clear();
        } catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                this.clear(0);
            } else {
                throw e;
            }
        }
        return this;
    }

    public Element sendKeys(String val, int... retries) {
        try {
            try {
                this.element().sendKeys(val);
            } catch (Exception e) {
                if (!(retries.length > 0 && retries[0] == 0)) {
                    this.refind(retries);
                    this.sendKeys(val, 0);
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            if (checkSendKeysJS()) {
                sendKeysJS(val);
            }
        }
        return this;
    }

    public Element click(int... retries) {
        try {
            try {
                this.element().click();
            } catch (Exception e) {
                if (!(retries.length > 0 && retries[0] == 0)) {
                    this.refind(retries);
                    this.click(0);
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            if (checkClickJS()) {
                clickJS();
            }
        }
        return this;
    }

    /**
     * send text using character chord to overwrite field
     */
    public Element sendKeysChord(String val, int... retries) {
        try {
            this.element.sendKeys(Keys.chord(Keys.CONTROL, "a"), val);
        } catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                this.sendKeysChord(val, 0);
            } else {
                throw e;
            }
        }
        return this;
    }

    /**
     * send text using character chord to overwrite field
     */
    public Element sendKeysChord(Keys key, int... retries) {
        try {
            this.element.sendKeys(Keys.chord(Keys.CONTROL, "a"), key);
        } catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                this.sendKeysChord(key, 0);
            } else {
                throw e;
            }
        }
        return this;
    }

    /**
     * mimic hitting the enter key
     */
    public Element sendEnter(int... retries) {
        try {
            this.element.sendKeys(Keys.ENTER);
        } catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                this.sendEnter(0);
            } else {
                throw e;
            }
        }
        return this;
    }

    /**
     * select action for checkboxes / radio buttons
     */
    public Element select(Boolean val, int... retries) {
        try {
            if (this.element.isSelected() != val)
                this.element.click();
        } catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                this.select(val, 0);
            } else {
                throw e;
            }
        }
        return this;
    }

    /**
     * select action for checkboxes / radio buttons
     */
    public Element select(int... retries) {
        try {
            if (!this.element.isSelected())
                this.element.click();
        } catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                this.select(0);
            } else {
                throw e;
            }
        }
        return this;
    }

    /**
     * deselect action for checkboxes / radio buttons
     */
    public Element unselect(int... retries) {
        try {
            if (this.element.isSelected())
                this.element.click();
        } catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                this.unselect(0);
            } else {
                throw e;
            }
        }
        return this;
    }

    /**
     * Return select object for a WebElement
     */
    public Select dropdown(int... retries) {
        Select sel = null;
        try {
            sel = new Select(this.element);
        } catch (Exception e) {
            if (!(retries.length > 0 && retries[0] == 0)) {
                this.refind(retries);
                this.dropdown(0);
            } else {
                throw e;
            }
        }
        return sel;
    }

    /**
     * Performs mouse click action on the element using javascript where native click does not work
     */
    public Element clickJS() {
        JavascriptExecutor executor = (JavascriptExecutor) DriverFactory.getInstance().getDriver();
        executor.executeScript("arguments[0].click();", this.element);
        return this;
    }

    /**
     * send text using javascript
     */
    public Element sendKeysJS(String val) {
        JavascriptExecutor executor = (JavascriptExecutor) DriverFactory.getInstance().getDriver();
        executor.executeScript("arguments[0].setAttribute('value', '" + val + "')", this.element);
        return this;
    }

    /**
     * Return all options within a dropdown as string array
     */
    public List<String> getDropdownOptionsValues() {
        List<String> optionsText = new ArrayList<String>();
        List<WebElement> options = this.dropdown().getOptions();
        for (WebElement option : options) {
            optionsText.add(option.getAttribute("value"));
        }
        return optionsText;
    }

    /**
     * Return all options within a dropdown as string array
     */
    public List<String> getDropdownOptionsText() {
        List<String> optionsText = new ArrayList<String>();
        List<WebElement> options = this.dropdown().getOptions();
        for (WebElement option : options) {
            optionsText.add(option.getText());
        }
        return optionsText;
    }

    /**
     * Return all options groups within a dropdown as string array
     */
    public List<String> getDropdownOptGroupsText() {
        List<String> optGroupsText = new ArrayList<String>();
        List<WebElement> optGroups = this.element().findElements(By.tagName("optgroup"));
        for (WebElement optGroup : optGroups) {
            optGroupsText.add(optGroup.getText());
        }
        return optGroupsText;
    }

    /**
     * Return all options groups within a dropdown as list of elements
     */
    public List<WebElement> getDropdownOptGroupsElements() {
        List<WebElement> optGroups = this.element().findElements(By.tagName("optgroup"));
        return optGroups;
    }

    /**
     * Return all options within an option group of a dropdown as string array
     */
    public List<String> getDropdownOptionsTextWithinGroup(String group) {
        List<String> optionsText = new ArrayList<String>();
        List<WebElement> options = this.element().findElements(By.xpath("//optgroup[@label=" + group + "]/option"));
        for (WebElement option : options) {
            optionsText.add(option.getText());
        }
        return optionsText;
    }

    /**
     * Return all options within an option group of a dropdown as list of elements
     */
    public List<WebElement> getDropdownOptionsElementsWithinGroup(String group) {
        List<WebElement> options = this.element().findElements(By.xpath("//optgroup[@label=" + group + "]/option"));
        return options;
    }


    /**
     * Return all inner text within a list of elements as string array
     */
    public List<String> getAllText(List<Element> els) {
        List<String> elementsText = new ArrayList<String>();
        for (Element el : els) {
            elementsText.add(el.element().getText());
        }
        return elementsText;
    }

    /**
     * Performs mouse action move to element on the screen
     */
    public Element move() {
        Actions action = new Actions(DriverFactory.getInstance().getDriver());
        action.moveToElement(this.element).build().perform();
        return this;
    }

    /**
     * Performs mouse action move to a parent element on the screen, locate child element and click
     */
    public Element moveAndClick(WebElement elChild) {
        Actions action = new Actions(DriverFactory.getInstance().getDriver());
        action.moveToElement(this.element).build().perform();
        elChild.click();
        return this;
    }

    /**
     * Performs mouse action click and hold
     */
    public Element clickAndHold() {
        Actions action = new Actions(DriverFactory.getInstance().getDriver());
        action.clickAndHold(this.element).build().perform();
        return this;
    }

    /**
     * Performs mouse action release button
     */
    public Element release() {
        Actions action = new Actions(DriverFactory.getInstance().getDriver());
        action.release().build().perform();
        return this;
    }

    /**
     * Highlights an element with a blue border.....useful when debugging/taking screenshots
     */
    public Element highlight() {
        JavascriptExecutor js = (JavascriptExecutor) DriverFactory.getInstance().getDriver();
        String script = "arguments[0].style.border";
        String border = "3px solid blue";
        js.executeScript(script + "='" + border + "'", this.element);
        return this;
    }

    /**
     * Returns duration for specified waits
     */
    public static int getWaitDuration() {
        final int defaultWait = 10;
        int duration;
        try {
            duration = Integer.parseInt(System.getProperty("cukes.selenium.defaultWait"));
        } catch (Exception e) {
            duration = defaultWait;
        }
        return duration;
    }

    public static int getFindRetries() {
        final int defaultFindRetries = 10;
        int refind;
        try {
            refind = Integer.parseInt(System.getProperty("cukes.selenium.defaultFindRetries"));
        } catch (Exception e) {
            refind = defaultFindRetries;
        }
        return refind;
    }

    public Boolean checkClickJS() {
        return ThreadContext.getInstance().getSeleniumProps().getBoolean("clickUsesJavaScript." + ThreadContext.getInstance().getBrowserName().replaceAll("\\s", ""), false);
    }

    public Boolean checkSendKeysJS() {
        return ThreadContext.getInstance().getSeleniumProps().getBoolean("sendKeysUsesJavaScript." + ThreadContext.getInstance().getBrowserName().replaceAll("\\s", ""), false);
    }
}
