package uk.ndc.csa.utilities.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Apache POI 5 based Excel test-data reader. */
public final class ExcelHelper {
    private ExcelHelper() {
    }

    public static ArrayList<ArrayList<Object>> getDataAsArrayList(
            String filepath, String worksheet, String... recordSet) throws IOException {
        ArrayList<ArrayList<Object>> data = new ArrayList<>();
        try (FileInputStream input = new FileInputStream(filepath);
             XSSFWorkbook workbook = new XSSFWorkbook(input)) {
            XSSFSheet sheet = requireSheet(workbook, worksheet);
            int maxDataCount = 0;
            Iterator<Row> rows = sheet.iterator();
            while (rows.hasNext()) {
                Row row = rows.next();
                if (row.getRowNum() == 0) {
                    maxDataCount = row.getLastCellNum();
                    continue;
                }
                if (isRowEmpty(row)) {
                    break;
                }
                ArrayList<Object> values = new ArrayList<>();
                for (int column = 0; column < maxDataCount; column++) {
                    values.add(getCellData(row.getCell(column)));
                }
                if (recordSet.length == 0
                        || (values.get(0) != null && values.get(0).toString().equalsIgnoreCase(recordSet[0]))) {
                    data.add(values);
                }
            }
        }
        return data;
    }

    public static Map<String, LinkedHashMap<String, Object>> getDataAsMap(
            String filepath, String worksheet) throws IOException {
        Map<String, LinkedHashMap<String, Object>> dataMap = new LinkedHashMap<>();
        try (FileInputStream input = new FileInputStream(filepath);
             XSSFWorkbook workbook = new XSSFWorkbook(input)) {
            XSSFSheet sheet = requireSheet(workbook, worksheet);
            int lastRow = sheet.getLastRowNum();
            int currentRow = 0;
            while (currentRow <= lastRow) {
                Row header = sheet.getRow(currentRow);
                if (isRowEmpty(header)) {
                    currentRow++;
                    continue;
                }
                currentRow++;
                while (currentRow <= lastRow && !isRowEmpty(sheet.getRow(currentRow))) {
                    Row dataRow = sheet.getRow(currentRow);
                    Object group = getCellData(header.getCell(0));
                    Object rowKey = getCellData(dataRow.getCell(0));
                    LinkedHashMap<String, Object> values = new LinkedHashMap<>();
                    for (int column = 1; column < dataRow.getLastCellNum(); column++) {
                        Object key = getCellData(header.getCell(column));
                        Object value = getCellData(dataRow.getCell(column));
                        if (key != null && value != null) {
                            values.put(key.toString(), value);
                        }
                    }
                    dataMap.put(group + "." + rowKey, values);
                    currentRow++;
                }
            }
        }
        return dataMap;
    }

    public static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int column = Math.max(0, row.getFirstCellNum()); column < row.getLastCellNum(); column++) {
            Cell cell = row.getCell(column);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private static Object getCellData(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue())
                    : cell.getNumericCellValue();
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> cell.getCellFormula();
            case BLANK, _NONE, ERROR -> null;
        };
    }

    private static XSSFSheet requireSheet(XSSFWorkbook workbook, String worksheet) {
        XSSFSheet sheet = workbook.getSheet(worksheet);
        if (sheet == null) {
            throw new IllegalArgumentException("Excel worksheet not found: " + worksheet);
        }
        return sheet;
    }
}
