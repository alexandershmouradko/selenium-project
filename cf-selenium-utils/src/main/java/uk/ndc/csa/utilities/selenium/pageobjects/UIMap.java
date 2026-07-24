package uk.ndc.csa.utilities.selenium.pageobjects;

import org.openqa.selenium.By;

public class UIMap {
	
	private By by;
	private String type;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public By getBy() {
		return by;
	}

	public void setBy(By by){
		this.by = by;
	}
	
	

}
