package com.assignment.customer_batch_processor.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class FileConversionService {
    
    
    @Autowired
    private BatchJobService batchJobService;
    
    private final String uploadDir = "src/main/resources/uploads/";
    private final String csvDir = "src/main/resources/converted/";
    
    /**
     * Converts XLSX file to CSV and triggers batch processing
     * @param file XLSX file to convert
     * @return Path to the converted CSV file
     * @throws Exception if conversion or processing fails
     */
    public String convertXlsxToCsv(MultipartFile file) throws Exception {
        
        // Create directories if they don't exist
        createDirectories();
        
        // Save the uploaded XLSX file
        String xlsxFilePath = saveUploadedFile(file);
        
        // Convert XLSX to CSV

        return performXlsxToCsvConversion(xlsxFilePath);
    }
    
    /**
     * Creates necessary directories for file storage
     */
    private void createDirectories() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        Path csvPath = Paths.get(csvDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadDir);
        }
        
        if (!Files.exists(csvPath)) {
            Files.createDirectories(csvPath);
            log.info("Created CSV directory: {}", csvDir);
        }
    }
    
    /**
     * Saves the uploaded XLSX file to the uploads directory
     */
    private String saveUploadedFile(MultipartFile file) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = timestamp + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;
        
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(file.getBytes());
        }
        
        log.info("XLSX file saved: {}", filePath);
        return filePath;
    }
    
    /**
     * Converts XLSX file to CSV format
     */
    private String performXlsxToCsvConversion(String xlsxFilePath) throws Exception {
        
        File xlsxFile = new File(xlsxFilePath);
        String csvFileName = xlsxFile.getName().replace(".xlsx", ".csv");
        String csvFilePath = csvDir + csvFileName;
        
        log.info("Starting conversion from {} to {}", xlsxFilePath, csvFilePath);
        
        try (FileInputStream fis = new FileInputStream(xlsxFile);
             XSSFWorkbook workbook = new XSSFWorkbook(fis);
             PrintWriter csvWriter = new PrintWriter(new FileWriter(csvFilePath))) {
            
            // Get the first sheet (assuming customer data is in first sheet)
            Sheet sheet = workbook.getSheetAt(0);
            
            int totalRows = sheet.getLastRowNum() + 1;
            log.info("Total rows in XLSX file: {}", totalRows);
            
            // Process each row
            for (Row row : sheet) {
                StringBuilder csvRow = new StringBuilder();
                
                // Process each cell in the row
                int cellCount = 0;
                for (Cell cell : row) {
                    if (cellCount > 0) {
                        csvRow.append(",");
                    }
                    
                    String cellValue = getCellValueAsString(cell);
                    
                    // Escape commas and quotes in cell values
                    if (cellValue.contains(",") || cellValue.contains("\"") || cellValue.contains("\n")) {
                        cellValue = "\"" + cellValue.replace("\"", "\"\"") + "\"";
                    }
                    
                    csvRow.append(cellValue);
                    cellCount++;
                }
                
                csvWriter.println(csvRow.toString());
                
                // Log progress for large files
                if (row.getRowNum() % 10000 == 0 && row.getRowNum() > 0) {
                    log.info("Converted {} rows to CSV", row.getRowNum());
                }
            }
            
            log.info("Successfully converted XLSX to CSV. Total rows: {}", totalRows);
            
        } catch (Exception e) {
            log.error("Error converting XLSX to CSV: {}", e.getMessage());
            throw new Exception("Failed to convert XLSX to CSV", e);
        }
        
        return csvFilePath;
    }
    
    /**
     * Gets cell value as string regardless of cell type
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Handle numeric values (remove decimal if it's a whole number)
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue().trim();
                }
            case BLANK:
            case _NONE:
            default:
                return "";
        }
    }
}