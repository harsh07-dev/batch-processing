//package com.assignment.customer_batch_processor.Controller;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api")
//public class CustomerController {
//
//	private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
//
//	@PostMapping("/upload-xlsx")
//	public ResponseEntity<String> handleExcelUpload(
//			@RequestParam(value = "file", required = false) MultipartFile file) {
//
//		if (file == null || file.isEmpty()) {
//			return new ResponseEntity<>("File is not present. Please upload a file.", HttpStatus.BAD_REQUEST);
//		}
//
//		try {
//			// Creates a unique temporary file with a .xlsx extension
//			String uploadDir="src/main/resources/uploads";
//			File directory=new File(uploadDir);
//			if (!directory.exists()) {
//				directory.mkdir();
//			}
//			 File savedFile = new File(directory, file.getOriginalFilename());
//
//			// Writes the binary content of the uploaded file to the temporary file
//			try (FileOutputStream fos = new FileOutputStream(savedFile)) {
//				fos.write(file.getBytes());
//			}
//
//			return new ResponseEntity<>("File successfully uploaded to: " + savedFile.getAbsolutePath(), HttpStatus.OK);
//		} catch (IOException e) {
//			// Handle any exceptions that occur during file creation or writing
//			throw new RuntimeException("Failed to save uploaded file.", e);
//		} catch (Exception e) {
//			logger.error("Failed to save file", e);
//			return new ResponseEntity<>("Failed to save file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}	
//		
//	
//	}
//}

package com.assignment.customer_batch_processor.Controller;

import com.assignment.customer_batch_processor.service.BatchJobService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.assignment.customer_batch_processor.service.FileConversionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/batch")
public class CustomerController {

    @Autowired
    private BatchJobService batchJobService;

    @Autowired
    private FileConversionService fileConversionService;
    
    @PostMapping("/upload")
    public ResponseEntity<Object> handleExcelUpload(
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        if (file == null || file.isEmpty()) {
            return new ResponseEntity<>("File is not present. Please upload a file.", HttpStatus.BAD_REQUEST);
        }
        
        // Validate file extension
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".xlsx")) {
            return new ResponseEntity<>("Invalid file type. Only .xlsx files are allowed.", HttpStatus.BAD_REQUEST);
        }
        
        try {
            // Call service to convert XLSX to CSV
            String csvFilePath = fileConversionService.convertXlsxToCsv(file);
            
            log.info("Successfully converted {} to CSV", fileName);

            JobExecution jobExecution = batchJobService.processCustomerFile(csvFilePath);

            Map<String,Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File accepted and processing started");
//            response.put("csvFilePath", csvFilePath);
            response.put("jobId", jobExecution.getId());
            response.put("status", jobExecution.getStatus().toString());
            if(jobExecution.getStatus().equals(BatchStatus.FAILED)) {
                List<Throwable> failureExceptions = jobExecution.getAllFailureExceptions();
                String errorMessage = "Job failed";

                if (!failureExceptions.isEmpty()) {
                    Throwable rootCause = failureExceptions.getFirst();
                    // Get the actual cause message, not the wrapper
                    while (rootCause.getCause() != null) {
                        rootCause = rootCause.getCause();
                    }
                    errorMessage = rootCause.getMessage();
                }

                Map<String,Object> errorResponse = new HashMap<>();

                errorResponse.put("status","FAILED");
                String retryMessage = " Please fix the file and re-upload";
                errorResponse.put("message", errorMessage + retryMessage);

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
            }
//            response.put("jobDetails", batchJobService.getJobExecutionStatus(jobExecution));

            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Error processing file upload: {}", e.getMessage());
            Map<String,Object> response = new HashMap<>();

            response.put("status","FAILED");
            response.put("message", e.getMessage());

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




}
