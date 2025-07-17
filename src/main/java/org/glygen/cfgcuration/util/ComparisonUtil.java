package org.glygen.cfgcuration.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.glygen.cfgcuration.model.mapping.Mapping;
import org.glygen.cfgcuration.model.mapping.MappingDisease;
import org.glygen.cfgcuration.model.mapping.MappingOrgan;
import org.glygen.cfgcuration.model.mapping.MappingScientificName;
import org.glygen.cfgcuration.model.mapping.MappingTissue;
import org.glygen.cfgcuration.service.CFGCurationService;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class ComparisonUtil {
	
	static Logger logger = org.slf4j.LoggerFactory.getLogger(ComparisonUtil.class);

	public List<Mapping> compareFiles(List<String> fileList, String tablename) {
		List<Mapping> processed = new ArrayList<>();
		List<Mapping> agreements = new ArrayList<Mapping>();
		List<Disagreement> disagreements = new ArrayList<>();
		String first = fileList.get(0);
		File file1 = new File (first);
		try {
			Workbook workbook = WorkbookFactory.create(file1);
			Sheet mappings = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = mappings.iterator();
			int count = 0;
	        while (rowIterator.hasNext()) {
	            Row row = rowIterator.next();
	            if (count == 0) {
	            	count = 1;
	            	continue;
	            } else {
	            	Disagreement dis = new Disagreement();
	            	String id = null;
	            	Cell idCell = row.getCell(0);
	            	if (idCell == null) {
	            		logger.warn("ID is empty for row " + row.getRowNum() + " exiting!");
	            		break;
	            	}
	            	if (idCell.getCellType() == CellType.NUMERIC) {
	            		id = (int)idCell.getNumericCellValue() + "";
	            	} else {
	            		id = idCell.getStringCellValue();
	            	}
	            	dis.id = id;
	            	String namespaceName = row.getCell(3).getStringCellValue();
	            	String name = row.getCell(2).getStringCellValue();
	            	Cell mappingCell = row.getCell(5);
	            	String mappingName = null;
	            	if (mappingCell != null) {
	            		mappingName = mappingCell.getStringCellValue();
	            	}
	            	String rank = null;
    				if (row.getCell(6) != null && row.getCell(6).getCellType() != CellType.NUMERIC) {
    					rank = row.getCell(6).getStringCellValue();
    				}
	            	dis.name = name;
	            	Cell namespaceIdCell = row.getCell(4);
	            	String namespaceId = null;
	            	if (namespaceIdCell.getCellType() == CellType.NUMERIC) {
	            		namespaceId = (int)namespaceIdCell.getNumericCellValue() + "";
	            	} else {
	            		namespaceId = namespaceIdCell.getStringCellValue();
	            	}
	            	if (!namespaceName.isEmpty() || !namespaceId.isEmpty()) {
	            		// check if it agrees with other files
	            		boolean matchedAll = true;
	            		dis.namespaceIds.add(namespaceId);
    					dis.namespaceNames.add(namespaceName);
    					dis.mappingNames.add(mappingName);
    					dis.ranks.add(rank);
	            		for (int i=1; i < fileList.size(); i++) {
	            			Mapping matched = findInFile (fileList.get(i), id, namespaceId, namespaceName, tablename);
	            			if (matched == null) {
	            				logger.error("Cannot find the matching row for " + id + " in file " + fileList.get(i));
	            				continue;
	            			} else {
	            				if (!matched.getNamespaceId().equals(namespaceId)) {
	            					matchedAll = false;
	            				}
            					dis.namespaceIds.add(matched.getNamespaceId());
            					dis.namespaceNames.add(matched.getNamespaceName());
            					dis.mappingNames.add(matched.getMappingName());
            					if (matched instanceof MappingScientificName) {
            						dis.ranks.add(((MappingScientificName) matched).getRank());
            					} else {
            						dis.ranks.add(null);
            					}
	            			}
	            		}
	            		if (matchedAll) {
            				Mapping mapping = createMapping (tablename, id, namespaceName, namespaceId, mappingName, rank);
            				mapping.setName(name);
            				agreements.add(mapping);
            				processed.add(mapping);
            			} else {
            				// update progress status
            				Mapping mapping = createMapping(tablename, id, null, null, null, null);
            				mapping.setName(name);
            				disagreements.add(dis);
            				processed.add(mapping);
            			}
	            	}
	            }
	        }
	        
	        List<String[]> rows = new ArrayList<>();
			List<String[]> rows2 = new ArrayList<String[]>();
			String[] header = {"ID", "name", "namespacename", "namespaceid", "mappingname", "rank"};
			rows.add(header);
			
			for (Mapping m: agreements) {
				String[] row = new String[6];
				row[0] = m.getId() + "";
				row[1] = m.getName();
				row[2] = m.getNamespaceName();
				row[3] = m.getNamespaceId();
				row[4] = m.getMappingName();
				if (m instanceof MappingScientificName) {
					row[5] = ((MappingScientificName) m).getRank();
				} 
				rows.add(row);
			}
			
			int i=0;
			for (Disagreement m: disagreements) {
				if (i == 0) {
					List<String> header2 = new ArrayList<>(Arrays.asList(header));
					int fileNo = fileList.size();
					for (int j=1; j < fileNo; j++) {
						header2.add("namespacename" + (j+1));
						header2.add("namespaceid" + (j+1));
						header2.add("mappingname" + (j+1));
						header2.add("rank" + (j+1));
					}
					rows2.add(header2.toArray(new String[0]));
					i++;
				} 
				String[] row = new String[2+4*m.namespaceIds.size()];
				row[0] = m.id + "";
				row[1] = m.name;
				for (int j=0; j < fileList.size(); j++) {
					int index = 2+4*j;
					row[index] = m.namespaceNames.get(j);
					row[index+1] = m.namespaceIds.get(j);
					row[index+2] = m.mappingNames.get(j);
					row[index+3] = m.ranks.get(j);
				}
				rows2.add(row);
			}
			
			CFGCurationService.writeToExcel(rows, "Agreements", "Comparison" + tablename + new java.util.Date() + ".xlsx", rows2, "Disagreements");
			return processed;
		} catch (EncryptedDocumentException | IOException e) {
			logger.error("Error comparing files", e);
		} 
		
		return null;
	}

	private Mapping createMapping(String tablename, String id, String namespaceName, String namespaceId,
			String mappingName, String rank) {
		
		if (tablename.equalsIgnoreCase("mapping_disease")) {
			Mapping mapping = new MappingDisease();
			if (namespaceName == null && namespaceId == null) {
				mapping.setId(Long.parseLong(id));
				mapping.setInProgress(true);
			} else {
				mapping.setNamespaceId(namespaceId);
				mapping.setId(Long.parseLong(id));
				mapping.setNamespaceName(namespaceName);
				mapping.setMappingName(mappingName);
			}
			return mapping;
		} else if (tablename.equalsIgnoreCase("mapping_tissue")) {
			Mapping mapping = new MappingTissue();
			if (namespaceName == null && namespaceId == null) {
				mapping.setId(Long.parseLong(id));
				mapping.setInProgress(true);
			} else {
				mapping.setNamespaceId(namespaceId);
				mapping.setId(Long.parseLong(id));
				mapping.setNamespaceName(namespaceName);
				mapping.setMappingName(mappingName);
			}
			return mapping;
		} else if (tablename.equalsIgnoreCase("mapping_organ")) {
			Mapping mapping = new MappingOrgan();
			if (namespaceName == null && namespaceId == null) {
				mapping.setId(Long.parseLong(id));
				mapping.setInProgress(true);
			} else {
				mapping.setNamespaceId(namespaceId);
				mapping.setId(Long.parseLong(id));
				mapping.setNamespaceName(namespaceName);
				mapping.setMappingName(mappingName);
			}
			return mapping;
		} else if (tablename.equalsIgnoreCase("mapping_scientificname")) {
			Mapping mapping = new MappingScientificName();
			if (namespaceName == null && namespaceId == null) {
				mapping.setId(Long.parseLong(id));
				mapping.setInProgress(true);
			} else {
				mapping.setNamespaceId(namespaceId);
				mapping.setId(Long.parseLong(id));
				mapping.setNamespaceName(namespaceName);
				mapping.setMappingName(mappingName);
				((MappingScientificName) mapping).setRank(rank);
			}
			return mapping;
		} 
		return null;
	}

	private Mapping findInFile(String filename, String id, String namespaceId, String namespaceName, String tablename) throws EncryptedDocumentException, IOException {
		File file = new File (filename);
		
		Workbook workbook = WorkbookFactory.create(file);
		Sheet mappings = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = mappings.iterator();
		int count = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if (count == 0) {
            	count = 1;
            	continue;
            } else {
            	String idInFile = null;
            	Cell idCell = row.getCell(0);
            	if (idCell == null) {
            		logger.warn("ID is empty for row " + row.getRowNum() + " of file " + filename);
            		break;
            	}
            	if (idCell.getCellType() == CellType.NUMERIC) {
            		idInFile = (int)idCell.getNumericCellValue() + "";
            	} else {
            		idInFile = idCell.getStringCellValue();
            	}
            	if (idInFile.equalsIgnoreCase(id)) {
            		String namespaceNameInFile = row.getCell(3).getStringCellValue();
	            	Cell namespaceIdCell = row.getCell(4);
	            	String namespaceIdInFile = null;
	            	if (namespaceIdCell.getCellType() == CellType.NUMERIC) {
	            		namespaceIdInFile = (int)namespaceIdCell.getNumericCellValue() + "";
	            	} else {
	            		namespaceIdInFile = namespaceIdCell.getStringCellValue();
	            	}
	            	Cell mappingCell = row.getCell(5);
	            	String mappingName = null;
	            	if (mappingCell != null) {
	            		mappingName = mappingCell.getStringCellValue();
	            	}
	            	String rank = null;
    				if (row.getCell(6) != null && row.getCell(6).getCellType() != CellType.NUMERIC) {
    					rank = row.getCell(6).getStringCellValue();
    				}
	            	
	            	/*if (namespaceName != null && namespaceName.equalsIgnoreCase(namespaceNameInFile)) {
	            		return createMapping(tablename, id, namespaceName, namespaceId, mappingName, rank);
	            		
	            	} */
	            	if (namespaceId != null && namespaceId.equalsIgnoreCase(namespaceIdInFile)) {
	            		return createMapping(tablename, id, namespaceName, namespaceId, mappingName, rank);
	            	}       	
	            	return createMapping (tablename, id, namespaceNameInFile, namespaceIdInFile, mappingName, rank);
            	}
            }
        }
        return null;
	}
	
	class Disagreement {
		String id;
		String name;
		List<String> namespaceNames = new ArrayList<>();
		List<String> namespaceIds = new ArrayList<>();
		List<String> mappingNames = new ArrayList<>();
		List<String> ranks = new ArrayList<>();
	}
}
