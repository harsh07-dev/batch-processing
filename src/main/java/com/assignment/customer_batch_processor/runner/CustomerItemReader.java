//package com.assignment.customer_batch_processor.runner;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class BatchJobRunner implements CommandLineRunner {
//    
//    private static final Logger logger = LoggerFactory.getLogger(BatchJobRunner.class);
//    
//    @Autowired
//    private JobLauncher jobLauncher;
//    
//    @Autowired
//    private Job csvReadingJob;
//    
//    @Override
//    public void run(String... args) throws Exception {
//        // Check if file path is provided as argument
//        String filePath;
//        if (args.length > 0) {
//            filePath = args[0];
//        } else {
//            // Default file path - update this to your CSV file location
//            filePath = "src/main/resources/uploads/TestFile.csv";
//        }
//        
//        logger.info("Starting batch job with file path: {}", filePath);
//        
//        // Build job parameters
//        JobParameters jobParameters = new JobParametersBuilder()
//                .addString("filePath", filePath)
//                .addLong("timestamp", System.currentTimeMillis()) // Unique parameter for job restart
//                .toJobParameters();
//        
//        try {
//            // Launch the job
//            var jobExecution = jobLauncher.run(csvReadingJob, jobParameters);
//            logger.info("Job execution status: {}", jobExecution.getStatus());
//            //logger.info("Job completed in: {} ms", jobExecution.getEndTime().getTime() - jobExecution.getStartTime().getTime());
//        } catch (Exception e) {
//            logger.error("Job execution failed", e);
//            throw e;
//        }
//    }
//}



package com.assignment.customer_batch_processor.runner;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * READER COMPONENT
 * 
 * Responsibility:
 * 1. Read CSV file line by line
 * 2. Parse each line into Customer object
 * 3. Handle file format and structure
 * 4. Skip header rows
 * 5. Provide data to Processor
 */
@Component
@StepScope
public class CustomerItemReader implements ItemReader<Customer> {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerItemReader.class);
    
    private FlatFileItemReader<Customer> flatFileItemReader;
    
    //@Value("#{jobParameters['filePath']}")
    private String filePath="src/main/resources/uploads/TestFile.csv";
    
    private int readCount = 0;
    
    @PostConstruct
    public void initialize() {
        logger.info("READER: Initializing CSV file reader for: {}", filePath);
        
        flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("customerCsvReader");
        
        // Set file resource
        if (filePath != null) {
            flatFileItemReader.setResource(new FileSystemResource(filePath));
        }
        
        // Configure line mapper
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
        
        // Configure tokenizer (CSV parsing)
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("name", "email", "phoneNumber", "aadhaarNumber", "panNumber", "state", "city");
        tokenizer.setDelimiter(",");
        tokenizer.setQuoteCharacter('"');
        tokenizer.setStrict(false); // Handle missing columns gracefully
        
        // Configure field mapper (CSV → Object mapping)
        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);
        fieldSetMapper.setDistanceLimit(0); // Exact field matching
        
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        
        flatFileItemReader.setLineMapper(lineMapper);
        flatFileItemReader.setLinesToSkip(1); // Skip CSV header
        
        // Log skipped header
        flatFileItemReader.setSkippedLinesCallback(line -> {
            logger.debug("📋 READER: Skipped header line: {}", line);
        });
        
        logger.info("✅ READER: CSV file reader configured successfully");
    }
    
    @Override
    public Customer read() throws Exception {
        // Ensure reader is initialized
        if (flatFileItemReader == null) {
            initialize();
        }
        
        // Read next customer from CSV
        Customer customer = flatFileItemReader.read();
        
        if (customer != null) {
            readCount++;
            
            // Log progress for large files
            if (readCount % 5000 == 0) {
                logger.info("📖 READER: Read {} customers from CSV file", readCount);
            }
            
            logger.debug("📝 READER: Read customer - Name: {}, Email: {}", 
                        customer.getName(), customer.getEmail());
        } else {
            logger.info("🏁 READER: Finished reading CSV file. Total customers read: {}", readCount);
        }
        
        return customer;
    }
    
    /**
     * Get total number of records read so far
     */
    public int getReadCount() {
        return readCount;
    }
    
    /**
     * Reset reader state (useful for testing)
     */
    public void reset() {
        readCount = 0;
        if (flatFileItemReader != null) {
            try {
                flatFileItemReader.close();
            } catch (Exception e) {
                logger.warn("Error closing file reader: {}", e.getMessage());
            }
        }
    }
}