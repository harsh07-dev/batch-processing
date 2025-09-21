package com.assignment.customer_batch_processor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;

@Service
@Slf4j
public class BatchJobService {
    
    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    @Qualifier("csvReadingJob")
    private Job csvReadingJob;
    
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
    
    /**
     * Get job execution status details
     */
    public String getJobExecutionStatus(JobExecution jobExecution) {
        if (jobExecution == null) {
            return "Job execution is null";
        }
        
        StringBuilder status = new StringBuilder();
        status.append("Job Name: ").append(jobExecution.getJobInstance().getJobName()).append("\n");
        status.append("Status: ").append(jobExecution.getStatus()).append("\n");
        status.append("Exit Status: ").append(jobExecution.getExitStatus()).append("\n");
        status.append("Start Time: ").append(jobExecution.getStartTime()).append("\n");
        status.append("End Time: ").append(jobExecution.getEndTime()).append("\n");
        
        if (jobExecution.getStepExecutions() != null) {
            jobExecution.getStepExecutions().forEach(stepExecution -> {
                status.append("Step: ").append(stepExecution.getStepName()).append(" - ");
                status.append("Read Count: ").append(stepExecution.getReadCount()).append(", ");
                status.append("Write Count: ").append(stepExecution.getWriteCount()).append(", ");
                status.append("Skip Count: ").append(stepExecution.getSkipCount()).append("\n");
            });
        }
        
        return status.toString();
    }
}