package com.roy.football.match.OFN.out;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.roy.football.match.OFN.out.header.HeaderConfiguration;
import com.roy.football.match.OFN.out.header.HeaderConfigurationManager;
import com.roy.football.match.base.TeamLabel;
import com.roy.football.match.base.TeamLevel;
import com.roy.football.match.util.DateUtil;

public class PoiWriter <T> implements Writer <T>{
	private final String SHEET_NAME = "OFN_Match";
	
	public PoiWriter (Class<T> clazz) {
		this.clazz = clazz;
	}
	
	public PoiWriter (Class<T> clazz, XSSFWorkbook workBook) {
		this.clazz = clazz;
		this.workBook = workBook;
	}
	
	public void write (List <T> elements, XSSFWorkbook wb) {
		this.workBook = wb;
		Sheet sheet = workBook.createSheet(SHEET_NAME);
		
		init(workBook);
		writeHeader(sheet);
		writeBody(sheet, elements);
		sheet.createFreezePane(0, 1, 0, 1);
	}

	public void write (List <T> elements) {
		Sheet sheet = workBook.createSheet(SHEET_NAME);
		
		init(workBook);
		writeHeader(sheet);
		writeBody(sheet, elements);
	}
	
	private void init (Workbook workBook) {
		headerConfigs = HeaderConfigurationManager.getHeaderConfigurations(clazz);

		headerStyle = workBook.createCellStyle();
		headerStyle.setAlignment(CellStyle.ALIGN_LEFT);
		headerStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headerStyle.setWrapText(true);
		Font ft = workBook.createFont();
		ft.setFontName("Arial");
		ft.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerStyle.setFont(ft);
		
		bodyStyle = workBook.createCellStyle();
		bodyStyle.setAlignment(CellStyle.ALIGN_LEFT);
		bodyStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		bodyStyle.setWrapText(true);
		
		dateCellStyle = workBook.createCellStyle();
		dateCellStyle.cloneStyleFrom(bodyStyle);
		CreationHelper createHelper = workBook.getCreationHelper();
		dateCellStyle.setDataFormat(
			    createHelper.createDataFormat().getFormat(DateUtil.simple_date_format_dash));
	}
	
	private void writeHeader (Sheet sheet) {
		Row hRow = sheet.createRow(0);

		for (int i=0; i < headerConfigs.size(); i++) {
			HeaderConfiguration hc = headerConfigs.get(i);
			Cell cell = hRow.createCell(i);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(hc.getTitle());
			int order = hc.getOrder();

			if (order ==160) {
				sheet.setColumnWidth(i, 4 * 512);
			}  else if (order == 150) {
				sheet.setColumnWidth(i, 5 * 512);
			} else if (order == 10 || order == 30 || order == 40
					|| order ==100 || order==110 || order==130 
					|| order ==170 || order ==180) {
				sheet.setColumnWidth(i, 6 * 512);
			} else if (order==148) {
				sheet.setColumnWidth(i, 7 * 512);
			} else if (order==20) {
				sheet.setColumnWidth(i, 8 * 512);
			} else if (order==190){
				sheet.setColumnWidth(i, 40 * 512);
			} else if (order==80 || order==60){
				sheet.setColumnWidth(i, 10 * 512);
			} else {
				sheet.setColumnWidth(i, 9 * 512);
			}
			
		}
	}
	
	private void writeBody (Sheet sheet, List <T> elements) {
		if (elements != null && elements.size() > 0) {
			int rowIndex = 1;
			for (T ele : elements) {
				Row row = sheet.createRow(rowIndex++);
				addRow(ele, row, false);
				row.setHeightInPoints(50);
			}
		}
	}
	
	protected void addRow(T obj, Row row, boolean isHeader){
		if (obj != null) {
			for (int i=0; i < headerConfigs.size(); i++) {
				HeaderConfiguration headerConfig = headerConfigs.get(i);
				String fieldName = headerConfig.getPropertyName();

				try {
					Object fieldVal = PropertyUtils.getProperty(obj, fieldName);
					Class <?> type = PropertyUtils.getPropertyType(obj, fieldName);
					addCell(fieldVal, type, row.createCell(i));
				} catch (Exception e) {
					System.out.println("Error to add field " + fieldName);
					e.printStackTrace();
				}
			}
		}
	}
	
	protected <E> void addCell(E cellData, Class <?> type, Cell cl){
		
		if (Number.class.isAssignableFrom(type)) {
			cl.setCellType(Cell.CELL_TYPE_NUMERIC);
			if (cellData != null) {
				cl.setCellValue(Double.parseDouble(cellData.toString()));
			}
		} else if (Boolean.class.isAssignableFrom(type)) {
			cl.setCellType(Cell.CELL_TYPE_BOOLEAN);
			if (cellData != null) {
				cl.setCellValue(Boolean.parseBoolean(cellData.toString()));
			}
		} else if (Date.class.isAssignableFrom(type)) {
			CellStyle celStyle = this.dateCellStyle;

			try {
				if(cellData != null) {
					cl.setCellValue(DateUtil.parseSimpleDateWithDash(cellData.toString()));
				}
			} catch (ParseException e) {
				// ignore...
			}
			
			cl.setCellStyle(celStyle);
		} else if(TeamLabel.class.isAssignableFrom(type)) {
			cl.setCellType(Cell.CELL_TYPE_STRING);
			if (cellData != null) {
				cl.setCellValue(((TeamLabel)cellData).name());
			}
		} else if(TeamLevel.class.isAssignableFrom(type)) {
			cl.setCellType(Cell.CELL_TYPE_STRING);
			if (cellData != null) {
				cl.setCellValue(((TeamLevel)cellData).name());
			}
		} else {
			cl.setCellType(Cell.CELL_TYPE_STRING);
			if (cellData != null) {
				cl.setCellValue(new XSSFRichTextString((String)cellData));
			}
		}
		
		cl.setCellStyle(bodyStyle);
	}
	
	private CellStyle headerStyle;
	private CellStyle bodyStyle;
	private CellStyle dateCellStyle;
	private List<HeaderConfiguration> headerConfigs;
	
	private Class<T> clazz;
	private XSSFWorkbook workBook;
}
