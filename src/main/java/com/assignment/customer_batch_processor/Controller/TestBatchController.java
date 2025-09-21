package com.assignment.customer_batch_processor.Controller;

import com.assignment.customer_batch_processor.service.BatchJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestBatchController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestBatchController.class);
    
    @Autowired
    private BatchJobService batchJobService;
    
    /**
     * Test endpoint to run batch job with existing CSV file
     */
    @PostMapping("/run-batch")
    public ResponseEntity<Map<String, Object>> testBatchJob() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Use the hardcoded CSV file path from your reader
            String csvFilePath = "src/main/resources/uploads/TestFile.csv";
            
            // Run the batch job
            JobExecution jobExecution = batchJobService.processCustomerFile(csvFilePath);
            
            // Prepare response
            response.put("success", true);
            response.put("message", "Batch job executed successfully");
            response.put("csvFilePath", csvFilePath);
            response.put("jobId", jobExecution.getId());
            response.put("jobStatus", jobExecution.getStatus().toString());
            response.put("jobDetails", batchJobService.getJobExecutionStatus(jobExecution));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error running test batch job: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}