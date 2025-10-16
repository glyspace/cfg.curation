package org.glygen.cfgcuration.util;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class UpdateGlycanRegistrationStatus {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println ("Enter the file name as an argument");
			System.exit(0);
		} 
		
		String filename = args[0];
		File file1 = new File (filename);
		try {
			HashMap<String, String> glycanStatus = new HashMap<>();
			ZipSecureFile.setMaxFileCount(10000);
			Workbook workbook = WorkbookFactory.create(file1);
			Sheet glycans = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = glycans.iterator();
			rowIterator.next(); // skip header line
			while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            Cell glytoucanIdCell = row.getCell(0);
	            Cell statusCell = row.getCell(1);
	            if (!glytoucanIdCell.getStringCellValue().isEmpty()) {
	            	if (!statusCell.getStringCellValue().equalsIgnoreCase("ALREADY_IN_GLYTOUCAN")) {
	            		glycanStatus.put (glytoucanIdCell.getStringCellValue(), statusCell.getStringCellValue());
	            	}
	            }
			}
			
	        String filePath = "updateGlycanStatus.sql";
	        try (FileWriter writer = new FileWriter(filePath)) {
	            for (Map.Entry<String, String> entry : glycanStatus.entrySet()) {
	                writer.append("update glycans set status='" + entry.getValue() + "' where glytoucanid = '" + entry.getKey() + "';\n");
	            }
	        }
		} catch (Exception e) {
			System.err.println ("Error reading the excel file: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
