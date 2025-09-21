//package com.assignment.customer_batch_processor.config;
//import com.assignment.customer_batch_processor.Customer_Entity.Customer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.batch.item.database.JpaItemWriter;
//import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
//import org.springframework.batch.item.file.FlatFileItemReader;
//import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
//import org.springframework.batch.item.file.mapping.DefaultLineMapper;
//import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
//import org.springframework.batch.item.support.CompositeItemWriter;
//import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import jakarta.persistence.EntityManagerFactory;
//import java.util.Arrays;
//
//@Configuration
//public class CsvReaderBatchConfig {
//    
//    private static final Logger logger = LoggerFactory.getLogger(CsvReaderBatchConfig.class);
//    
//    @Autowired
//    private EntityManagerFactory entityManagerFactory;
//    
//    @Bean
//    public Job csvReadingJob(JobRepository jobRepository, Step csvReadingStep) {
//        return new JobBuilder("csvReadingJob", jobRepository)
//                .start(csvReadingStep)
//                .build();
//    }
//    
//    @Bean
//    public Step csvReadingStep(JobRepository jobRepository, 
//                             PlatformTransactionManager transactionManager,
//                             ItemReader<Customer> csvItemReader,
//                             ItemProcessor<Customer, Customer> csvItemProcessor,
//                             ItemWriter<Customer> csvItemWriter) {
//        return new StepBuilder("csvReadingStep", jobRepository)
//                .<Customer, Customer>chunk(1000, transactionManager)
//                .reader(csvItemReader)
//                .processor(csvItemProcessor)
//                .writer(csvItemWriter)
//                .build();
//    }
//    
//    @Bean
//    @StepScope
//    public FlatFileItemReader<Customer> csvItemReader(@Value("#{jobParameters['filePath']}") String filePath) {
//        logger.info("Configuring CSV reader for file: {}", filePath);
//        
//        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();
//        reader.setName("csvItemReader");
//        reader.setResource(new FileSystemResource(filePath));
//        
//        // Configure line mapper
//        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
//        
//        // Configure tokenizer
//        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
//        tokenizer.setNames("name", "email", "phoneNumber", "aadhaarNumber", "panNumber", "state", "city");
//        tokenizer.setDelimiter(",");
//        tokenizer.setQuoteCharacter('"');
//        tokenizer.setStrict(false);
//        
//        // Configure field set mapper
//        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
//        fieldSetMapper.setTargetType(Customer.class);
//        fieldSetMapper.setDistanceLimit(0);
//        
//        lineMapper.setLineTokenizer(tokenizer);
//        lineMapper.setFieldSetMapper(fieldSetMapper);
//        
//        reader.setLineMapper(lineMapper);
//        reader.setLinesToSkip(1); // Skip header row
//        reader.setSkippedLinesCallback(line -> logger.debug("Skipped header line: {}", line));
//        
//        return reader;
//    }
//    
//    @Bean
//    public ItemProcessor<Customer, Customer> csvItemProcessor() {
//        return new ItemProcessor<Customer, Customer>() {
//            private int processedCount = 0;
//            
//            @Override
//            public Customer process(Customer customer) throws Exception {
//                processedCount++;
//                
//                // Log progress every 5000 records for large files
//                if (processedCount % 5000 == 0) {
//                    logger.info("Processed {} customers so far...", processedCount);
//                }
//                
//                // Basic data validation and cleanup
//                if (customer.getName() != null) {
//                    customer.setName(customer.getName().trim());
//                }
//                if (customer.getEmail() != null) {
//                    customer.setEmail(customer.getEmail().trim().toLowerCase());
//                }
//                if (customer.getPhoneNumber() != null) {
//                    customer.setPhoneNumber(customer.getPhoneNumber().trim());
//                }
//                if (customer.getAadhaarNumber() != null) {
//                    customer.setAadhaarNumber(customer.getAadhaarNumber().trim());
//                }
//                if (customer.getPanNumber() != null) {
//                    customer.setPanNumber(customer.getPanNumber().trim());
//                }
//                if (customer.getState() != null) {
//                    customer.setState(customer.getState().trim());
//                }
//                if (customer.getCity() != null) {
//                    customer.setCity(customer.getCity().trim());
//                }
//                
//                return customer; // Return the customer to pass to writer
//            }
//        };
//    }
//    
//    /**
//     * Composite Item Writer - writes to both database and console
//     * You can choose either database only, console only, or both
//     */
//    @Bean
//    public CompositeItemWriter<Customer> csvItemWriter() {
//        return new CompositeItemWriterBuilder<Customer>()
//                .delegates(Arrays.asList(
//                    databaseItemWriter(),
//                    consoleItemWriter()
//                ))
//                .build();
//    }
//    
//    /**
//     * Database Item Writer - saves customers to database using JPA
//     */
//    @Bean
//    public JpaItemWriter<Customer> databaseItemWriter() {
//        return new JpaItemWriterBuilder<Customer>()
//                .entityManagerFactory(entityManagerFactory)
//                .build();
//    }
//    
//    /**
//     * Console Item Writer - prints customer info to console (optional)
//     * Comment out the consoleItemWriter() from the composite writer above if you don't want console output
//     */
//    @Bean
//    public ItemWriter<Customer> consoleItemWriter() {
//        return new ItemWriter<Customer>() {
//            private int totalWritten = 0;
//            
//            @Override
//            public void write(org.springframework.batch.item.Chunk<? extends Customer> chunk) throws Exception {
//                totalWritten += chunk.size();
//                
//                // Log batch summary instead of individual records for better performance
//                logger.info("Saved batch of {} customers to database. Total processed: {}", 
//                    chunk.size(), totalWritten);
//                
//                // Optionally print first customer from each batch for verification
//                if (!chunk.isEmpty()) {
//                    Customer first = chunk.getItems().get(0);
//                    logger.info("Sample record - Name: {}, Email: {}, State: {}", 
//                        first.getName(), first.getEmail(), first.getState());
//                }
//            }
//        };
//    }
//}


package com.assignment.customer_batch_processor.config;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import com.assignment.customer_batch_processor.processor.CustomerItemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import java.util.Arrays;

@Configuration
public class CsvReaderBatchConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvReaderBatchConfig.class);
    
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    
    // ADDED: Inject your CustomerItemProcessor with validation and encryption
    @Autowired
    private CustomerItemProcessor customerItemProcessor;
    
    @Bean
    public Job csvReadingJob(JobRepository jobRepository, Step csvReadingStep) {
        logger.info("Creating CSV Reading Job with validation and encryption");
        return new JobBuilder("csvReadingJob", jobRepository)
                .start(csvReadingStep)
                .build();
    }
    
    @Bean
    public Step csvReadingStep(JobRepository jobRepository, 
                             PlatformTransactionManager transactionManager,
                             ItemReader<Customer> csvItemReader,
                             ItemProcessor<Customer, Customer> csvItemProcessor,
                             ItemWriter<Customer> csvItemWriter) {
        
        logger.info("Creating CSV Reading Step with chunk size: 1000");
        
        return new StepBuilder("csvReadingStep", jobRepository)
                .<Customer, Customer>chunk(1000, transactionManager)
                .reader(csvItemReader)
                .processor(csvItemProcessor) // Uses your CustomerItemProcessor
                .writer(csvItemWriter)
                
                // ADDED: Fault tolerance for invalid records
                .faultTolerant()
                .skipLimit(1000) // Skip up to 1000 invalid records
                .skip(Exception.class)
                
                .build();
    }
    
    @Bean
    @StepScope
    public FlatFileItemReader<Customer> csvItemReader(@Value("#{jobParameters['filePath']}") String filePath) {
        logger.info("Configuring CSV reader for file: {}", filePath);
        
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();
        reader.setName("csvItemReader");
        reader.setResource(new FileSystemResource(filePath));
        
        // Configure line mapper
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
        
        // Configure tokenizer
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("name", "email", "phoneNumber", "aadhaarNumber", "panNumber", "state", "city");
        tokenizer.setDelimiter(",");
        tokenizer.setQuoteCharacter('"');
        tokenizer.setStrict(false);
        
        // Configure field set mapper
        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);
        fieldSetMapper.setDistanceLimit(0);
        
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        
        reader.setLineMapper(lineMapper);
        reader.setLinesToSkip(1); // Skip header row
        reader.setSkippedLinesCallback(line -> logger.debug("Skipped header line: {}", line));
        
        return reader;
    }
    
    // UPDATED: Use your CustomerItemProcessor with validation and encryption
    @Bean
    public ItemProcessor<Customer, Customer> csvItemProcessor() {
        logger.info("Using CustomerItemProcessor with validation and encryption");
        return customerItemProcessor; // Your component with validation & encryption
    }
    
    /**
     * Composite Item Writer - writes to both database and console
     */
    @Bean
    public CompositeItemWriter<Customer> csvItemWriter() {
        return new CompositeItemWriterBuilder<Customer>()
                .delegates(Arrays.asList(
                    databaseItemWriter(),
                    consoleItemWriter()
                ))
                .build();
    }
    
    /**
     * Database Item Writer - saves customers to database using JPA
     */
    @Bean
    public JpaItemWriter<Customer> databaseItemWriter() {
        return new JpaItemWriterBuilder<Customer>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
    
    /**
     * Console Item Writer - prints customer info to console (optional)
     */
    @Bean
    public ItemWriter<Customer> consoleItemWriter() {
        return new ItemWriter<Customer>() {
            private int totalWritten = 0;
            
            @Override
            public void write(org.springframework.batch.item.Chunk<? extends Customer> chunk) throws Exception {
                totalWritten += chunk.size();
                
                // Log batch summary instead of individual records for better performance
                logger.info("Saved batch of {} customers to database. Total processed: {}", 
                    chunk.size(), totalWritten);
                
                // Optionally print first customer from each batch for verification
                if (!chunk.isEmpty()) {
                    Customer first = chunk.getItems().get(0);
                    logger.info("Sample record - ID: {}, Name: {}, Email: {}, State: {}", 
                        first.getId(), first.getName(), first.getEmail(), first.getState());
                }
            }
        };
    }
}