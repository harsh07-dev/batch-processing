package com.assignment.customer_batch_processor.config;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import com.assignment.customer_batch_processor.Utilities.CustomerItemProcessor;
import com.assignment.customer_batch_processor.Utilities.CustomerItemReader;
import com.assignment.customer_batch_processor.Utilities.CustomerItemWriter;
import com.assignment.customer_batch_processor.Utilities.NoOpItemProcessor;
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
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
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

    @Autowired
    CustomerItemReader customerItemReader;

    @Autowired
    NoOpItemProcessor noOpItemProcessor;


    
    @Bean
    public Job csvReadingJob(JobRepository jobRepository, Step csvReadingStep, Step validationStep) {
        log.debug("Creating CSV Reading Job with validation and encryption");
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
        log.debug("inside validationStep");
        return new StepBuilder("validationStep", jobRepository)
                .<Customer, Customer>chunk(2000, transactionManager)
                .reader(csvItemReader)
                .processor(csvItemProcessor) // Uses CustomerItemProcessor
                .writer(noOpWriter)
                .build();


    }


    /**
     * Retry csvReadingStep
     * runs after validation step.
     */
    @Bean
    public Step csvReadingStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             ItemReader<Customer> csvItemReader,
                             ItemProcessor<Customer, Customer> noOpProcess,
                             ItemWriter<Customer> csvItemWriter) {
        
        log.info("Creating CSV Reading Step with chunk size: 1000");


        return new StepBuilder("csvReadingStep", jobRepository)
                .<Customer, Customer>chunk(2000, transactionManager)
                .reader(csvItemReader)
                .processor(noOpProcess) // Uses your CustomerItemProcessor
                .writer(csvItemWriter)
                .faultTolerant()
                .noRetry(ValidationException.class)
                .retry(DataAccessException.class)
                .retry(RetryException.class)
                .retryLimit(3)
                .allowStartIfComplete(false)
                .startLimit(5)
                .build();
    }



    @Bean
    @StepScope
    public FlatFileItemReader<Customer> csvItemReader(@Value("#{jobParameters['filePath']}") String filePath) {

        return CustomerItemReader.customerFlatFileItemReader(filePath);
    }

    /**
     * this performs validation checks
     * @return Customer
     */
    @Bean
    public ItemProcessor<Customer, Customer> csvItemProcessor() {
        log.info("Using CustomerItemProcessor with validation and encryption");
        return customerItemProcessor; // Your component with validation & encryption
    }

    /**
     * this does not perform any validation checks again.
     * @return Customer
     */
    @Bean
    public ItemProcessor<Customer,Customer> noOpProcess() {
        return noOpItemProcessor;
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
public ItemWriter<Customer> databaseItemWriter()
{
    log.error("using customerItemWriter for database operations");
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

                    log.info("Saved batch of {} customers to database. Total processed: {} ",
                            chunk.size(), totalWritten);

                    if (!chunk.isEmpty()) {
                        Customer first = chunk.getItems().getFirst();
                        log.info("Sample record - ID: {}, Name: {}, Email: {}, State: {} ",
                                first.getId(), first.getName(), first.getEmail(), first.getState());
                    }
                }
            };
    }
}