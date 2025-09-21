package com.assignment.customer_batch_processor.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileConversionService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileConversionService.class);
    
    /**
     * Converts an uploaded XLSX file to CSV format
     * 
     * @param xlsxFile The uploaded XLSX MultipartFile
     * @return String path of the generated CSV file
     * @throws IOException if file processing fails
     */
    public String convertXlsxToCsv(MultipartFile xlsxFile) throws IOException {
        logger.info("Starting XLSX to CSV conversion for file: {}", xlsxFile.getOriginalFilename());
        
        // Create upload directory
        String uploadDir = "src/main/resources/uploads";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
            logger.info("Created upload directory: {}", uploadDir);
        }
        
        // Generate CSV file name
        String originalFileName = xlsxFile.getOriginalFilename();
        String csvFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.')) + ".csv";
        File csvFile = new File(directory, csvFileName);
        
        // Perform the conversion
        performConversion(xlsxFile, csvFile);
        
        logger.info("Successfully converted {} to {}", originalFileName, csvFileName);
        return csvFile.getAbsolutePath();
    }
    
    /**
     * Performs the actual XLSX to CSV conversion
     * 
     * @param xlsxFile Source XLSX file
     * @param csvFile Target CSV file
     * @throws IOException if conversion fails
     */
    private void performConversion(MultipartFile xlsxFile, File csvFile) throws IOException {
        logger.debug("Starting conversion process...");
        
        try (InputStream inputStream = xlsxFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream);
             PrintWriter csvWriter = new PrintWriter(new FileWriter(csvFile))) {
            
            // Get the first sheet
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            
            int rowCount = 0;
            int processedRows = 0;
            
            logger.info("Processing sheet: {} with {} rows", sheet.getSheetName(), sheet.getPhysicalNumberOfRows());
            
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowCount++;
                
                // Skip completely empty rows
                if (isRowEmpty(row)) {
                    logger.debug("Skipping empty row: {}", rowCount);
                    continue;
                }
                
                // Convert row to CSV format
                String csvRow = convertRowToCsv(row);
                csvWriter.println(csvRow);
                processedRows++;
                
                // Log progress for large files
                if (processedRows % 1000 == 0) {
                    logger.info("Processed {} rows...", processedRows);
                }
            }
            
            logger.info("Successfully converted XLSX to CSV. Total rows processed: {}", processedRows);
            
        } catch (Exception e) {
            logger.error("Error during XLSX to CSV conversion", e);
            throw new IOException("Failed to convert XLSX to CSV: " + e.getMessage(), e);
        }
    }
    
    /**
     * Converts a single Excel row to CSV format
     * 
     * @param row The Excel row to convert
     * @return String representation of the row in CSV format
     */
    private String convertRowToCsv(Row row) {
        StringBuilder csvRow = new StringBuilder();
        int lastCellNum = Math.max(7, row.getLastCellNum()); // Ensure at least 7 columns
        
        for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
            Cell cell = row.getCell(cellIndex);
            String cellValue = getCellValueAsString(cell);
            
            // Add comma separator (except for first column)
            if (cellIndex > 0) {
                csvRow.append(",");
            }
            
            // Handle CSV escaping for values containing commas, quotes, or newlines
            if (cellValue.contains(",") || cellValue.contains("\"") || cellValue.contains("\n")) {
                cellValue = "\"" + cellValue.replace("\"", "\"\"") + "\"";
            }
            
            csvRow.append(cellValue);
        }
        
        return csvRow.toString();
    }
    
    /**
     * Checks if an Excel row is completely empty
     * 
     * @param row The row to check
     * @return true if row is empty, false otherwise
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        
        for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
            Cell cell = row.getCell(cellIndex);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Extracts string value from an Excel cell handling different cell types
     * 
     * @param cell The Excel cell to read
     * @return String value of the cell
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        // Handle whole numbers without decimal points
                        double numericValue = cell.getNumericCellValue();
                        if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
                            return String.valueOf((long) numericValue);
                        } else {
                            return String.valueOf(numericValue);
                        }
                    }
                
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                
                case FORMULA:
                    try {
                        // Try to evaluate the formula
                        switch (cell.getCachedFormulaResultType()) {
                            case STRING:
                                return cell.getStringCellValue().trim();
                            case NUMERIC:
                                double formulaValue = cell.getNumericCellValue();
                                if (formulaValue == Math.floor(formulaValue) && !Double.isInfinite(formulaValue)) {
                                    return String.valueOf((long) formulaValue);
                                } else {
                                    return String.valueOf(formulaValue);
                                }
                            case BOOLEAN:
                                return String.valueOf(cell.getBooleanCellValue());
                            default:
                                return cell.getCellFormula();
                        }
                    } catch (Exception e) {
                        logger.warn("Could not evaluate formula in cell, returning formula string: {}", e.getMessage());
                        return cell.getCellFormula();
                    }
                
                case BLANK:
                case _NONE:
                default:
                    return "";
            }
        } catch (Exception e) {
            logger.warn("Error reading cell value: {}", e.getMessage());
            return "";
        }
    }
}