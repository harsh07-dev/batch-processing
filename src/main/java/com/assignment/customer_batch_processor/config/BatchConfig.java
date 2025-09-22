package com.assignment.customer_batch_processor.config;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import com.assignment.customer_batch_processor.Utilities.CustomerItemProcessor;
import com.assignment.customer_batch_processor.Utilities.CustomerItemWriter;
import com.assignment.customer_batch_processor.validator.RetryException;
import com.assignment.customer_batch_processor.validator.ValidationException;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

@Configuration
@Slf4j
public class BatchConfig {
    
    
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    
    @Autowired
    private CustomerItemProcessor customerItemProcessor;

    @Autowired
    private CustomerItemWriter customerItemWriter;
    
    @Bean
    public Job csvReadingJob(JobRepository jobRepository, Step csvReadingStep, Step validationStep) {
        log.info("Creating CSV Reading Job with validation and encryption");
        return new JobBuilder("csvReadingJob", jobRepository)
                .start(validationStep)
                .on("FAILED").fail()                 // Explicitly fail the job
                .on("COMPLETED").to(csvReadingStep)  // Continue to processing if validation passes
                .from(csvReadingStep)
                .on("*").end()                       // End job after processing
                .end()
                .build();
    }

    @Bean
    public Step validationStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               ItemReader<Customer> csvItemReader,
                               ItemProcessor<Customer, Customer> csvItemProcessor,
                               ItemWriter<Customer> noOpWriter) {
        log.info("inside validationStep");
        return new StepBuilder("validationStep", jobRepository)
                .<Customer, Customer>chunk(2000, transactionManager)
                .reader(csvItemReader)
                .processor(csvItemProcessor) // Uses your CustomerItemProcessor
                .writer(noOpWriter)
                .build();


    }
    
    @Bean
    public Step csvReadingStep(JobRepository jobRepository, 
                             PlatformTransactionManager transactionManager,
                             ItemReader<Customer> csvItemReader,
                             ItemProcessor<Customer, Customer> csvItemProcessor,
                             ItemWriter<Customer> csvItemWriter) {
        
        log.info("Creating CSV Reading Step with chunk size: 1000");


        return new StepBuilder("csvReadingStep", jobRepository)
                .<Customer, Customer>chunk(2000, transactionManager)
                .reader(csvItemReader)
                .processor(csvItemProcessor) // Uses your CustomerItemProcessor
                .writer(csvItemWriter)
                .faultTolerant()
                .noRetry(ValidationException.class)
                .retry(DataAccessResourceFailureException.class)
//                .retry(DataAccessException.class)
                .retry(RetryException.class)
                //.retryLimit(3)
                .retryLimit(2)
                .allowStartIfComplete(false)
                .startLimit(5)
                .build();





    }

    /**
     * This class is same as customer item reader class
     */
    /

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> csvItemReader(@Value("#{jobParameters['filePath']}") String filePath) {
        log.info("Configuring CSV reader for file: {}", filePath);

        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();
        reader.setName("csvItemReader");
        reader.setResource(new FileSystemResource(filePath));
        reader.setSaveState(true);

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
        reader.setSkippedLinesCallback(line -> log.info("Skipped header line: {}", line));

        return reader;
    }
    
    // UPDATED: Use your CustomerItemProcessor with validation and encryption
    @Bean
    public ItemProcessor<Customer, Customer> csvItemProcessor() {
        log.info("Using CustomerItemProcessor with validation and encryption");
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

    @Bean
    public ItemWriter<Customer> noOpWriter() {
        return customers -> {
            log.info("Validated {} customers successfully", customers.size());
            // Don't save anything - just validate
        };
    }

    @Bean
    public ItemWriter<Customer> noOpProcess() {
        return customers -> {
            log.info("Validated {} customers successfully", customers.size());
            // Don't save anything - just validate
        };
    }
    
    /**
     * Database Item Writer - saves customers to database using JPA
     */
//    @Bean
//    public JpaItemWriter<Customer> databaseItemWriter() {
//        return new JpaItemWriterBuilder<Customer>()
//                .entityManagerFactory(entityManagerFactory)
//                .build();
//    }
@Bean
public ItemWriter<Customer> databaseItemWriter()
{
    log.error("using customerItemwriter for database operations");
    return customerItemWriter;
}
    /**
     * Console Item Writer - prints customer info to console
     */
    @Bean
    public ItemWriter<Customer> consoleItemWriter() {
        return new ItemWriter<Customer>() {
            private int totalWritten = 0;
                @Override
                public void write(org.springframework.batch.item.Chunk<? extends Customer> chunk) throws Exception {
                    totalWritten += chunk.size();

                    // Log batch summary instead of individual records for better performance
                    log.info("Saved batch of {} customers to database. Total processed: {}",
                            chunk.size(), totalWritten);

                    // Optionally print first customer from each batch for verification
                    if (!chunk.isEmpty()) {
                        Customer first = chunk.getItems().get(0);
                        log.info("Sample record - ID: {}, Name: {}, Email: {}, State: {}",
                                first.getId(), first.getName(), first.getEmail(), first.getState());
                    }
                }
            };
    }
}