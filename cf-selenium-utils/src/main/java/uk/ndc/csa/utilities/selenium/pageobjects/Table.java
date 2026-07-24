package uk.ndc.csa.utilities.selenium.pageobjects;


import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/** 
 * Utility class providing set of selenium wrapper methods for interacting with simple html tables 
 */

public class Table extends Element{
	
	public Table(WebElement e) {
		super(e);
	}
	
	public Table(By by) {
		super(by);
	}

	/** Get count of number of table rows */
	public int getTableRowCount(){
		return getDataRowCount() + getHeadRowCount();
	}
	
	/** Get count of number of table header rows */
	public int getHeadRowCount(){
		return  this.findElements(By.tagName("th")).size();
	}
	
	/** Get count of number of table data rows */
	public int getDataRowCount(){
		return  this.findElements(By.tagName("tr")).size();
	}
	
	/** Get count of number of table header columns */
	public int getHeadColumnCount(int rowIndex){
		return getHeadRowElements(rowIndex).size();
	}
		
	/** Get count of number of table data columns */
	public int getDataColumnCount(int rowIndex){
		return getDataRowElements(rowIndex).size();
	}
	
	/** Get the list of header columns within a given row of a table */
	public List<Element> getHeadRowElements(int rowIndex){
		return this.findElements(By.tagName("tr")).get(rowIndex).findElements(By.tagName("th"));
	}

	/** Get the list of data columns within a given row of a table */
	public List<Element> getDataRowElements(int rowIndex){
		return this.findElements(By.tagName("tr")).get(rowIndex).findElements(By.tagName("td"));
	}
	
	/** Get all rows of a table */
	public List<Element> getAllRows(){
		return this.findElements(By.tagName("tr"));
	}
	
	/** Get table row based on row index */
	public Element getRow(int row){
    	return this.findElements(By.tagName("tr")).get(row);
    }
	
	/** Get table data cell based on row index and column index */
	public Element getDataCellElement(int rowIndex, int columnIndex){
		return getDataRowElements(rowIndex).get(columnIndex);
	}
	
	/** Get table header cell based on row index and column index */
	public Element getHeadCellElement(int rowIndex, int columnIndex){
		return getHeadRowElements(rowIndex).get(columnIndex);
	}
	
	/** Get table row based on matching a specified value against a value held in a given column */
	public Element tableGetRow(String val, int matchCol){
    	List<Element> rows = this.findElements(By.tagName("tr"));
		Element el = null;
		for (int i=1;i<rows.size();i++){
			if(rows.get(i).findElements(By.tagName("td")).get(matchCol).getText().equalsIgnoreCase(val)){
				el = rows.get(i);
				break;
			}
		}	
		return el;
    }
	
	/** Get all data held in a table and return as a string array */
	public ArrayList<ArrayList<String>> getTableAsArray(){
		ArrayList<ArrayList<String>> tabledata = new ArrayList<ArrayList<String>>();
		List<WebElement> rows = this.element().findElements(By.tagName("tr"));
		int numrows = rows.size();
		for (int i=0; i<numrows;i++){
			WebElement row = rows.get(i);
			List<WebElement> cols = row.findElements(By.tagName("td"));
			if (cols.size() > 0){
				ArrayList<String> rowdata = new ArrayList<String>();
				for (int j=0;j<cols.size();j++){
					rowdata.add(cols.get(j).getText().trim());
				}
				tabledata.add(rowdata);
			}	     
		}
		return tabledata;
		
		}
}
