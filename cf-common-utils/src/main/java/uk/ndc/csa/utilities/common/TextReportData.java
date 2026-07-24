package uk.ndc.csa.utilities.common;

import java.util.ArrayList;
import java.util.List;

//This class contains the string representative of the table
public class TextReportData {
	
	private String name;
	private String duration;
	private String status;
	private List<TextReportData> child;
	
	public TextReportData(String name, String status, String duration) {
		this.name = name;
		this.status = status;
		this.duration = duration;
		this.child = new ArrayList<TextReportData>();
	}
	
	public String getName() {
		return name;
	}
	
	public String getDuration() {
		return duration;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void addChild(TextReportData data) {
		child.add(data);
	}
	
	public List<TextReportData> getChildren(){
		return child;
	}
}
