package com.assignment.customer_batch_processor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class BatchJobService {
    
    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    @Qualifier("csvReadingJob")
    private Job csvReadingJob;

    @Autowired
    private JobExplorer jobExplorer;
    
    /**
     * Process a CSV file using Spring Batch
     */
    public JobExecution processCustomerFile(String filePath) throws Exception{
        try {
            log.info("Starting batch job for file: {}", filePath);
            
            // Validate file exists
            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("File not found: " + filePath);
            }
            
            long fileSizeKB = file.length() / 1024;
            log.info("Processing file of size: {} KB", fileSizeKB);
            
            // Build job parameters
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("filePath", filePath)
                    .addString("startTime", LocalDateTime.now().toString())
                    .addString("jobid" , UUID.randomUUID().toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            // Launch the job
            JobExecution jobExecution = jobLauncher.run(csvReadingJob, jobParameters);
            
            log.info("Batch job completed with status: {}", jobExecution.getStatus());
            log.info("Job execution summary: Exit Status = {}, Start Time = {}, End Time = {}", 
                       jobExecution.getExitStatus(), jobExecution.getStartTime(), jobExecution.getEndTime());
            
            return jobExecution;
            
        } catch (Exception e) {
            log.error("Error executing batch job for file {}: {}", filePath, e.getMessage());
            throw new RuntimeException("Batch job execution failed", e);
        }
    }

}