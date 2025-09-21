package com.assignment.customer_batch_processor.Utilities;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
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
@Slf4j
public class CustomerItemReader implements ItemReader<Customer> {
    
    private FlatFileItemReader<Customer> flatFileItemReader;
    
    //@Value("#{jobParameters['filePath']}")
    private String filePath="src/main/resources/uploads/TestFile.csv";

    /**
     * -- GETTER --
     *  Get total number of records read so far
     */
    @Getter
    private int readCount = 0;

    @PostConstruct
    public void initialize() {
        log.info("READER: Initializing CSV file reader for: {}", filePath);

        flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("customerCsvReader");

        if (filePath != null) {
            flatFileItemReader.setResource(new FileSystemResource(filePath));
        }

        // LineMapper that maps based on column names in the header row
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        // Use a tokenizer that looks at the header row
        DelimitedLineTokenizer tokenizer =
                new org.springframework.batch.item.file.transform.DelimitedLineTokenizer() {
                    @Override
                    public FieldSet tokenize(String line) {
                        return super.tokenize(line);
                    }
                };

        tokenizer.setDelimiter(",");
        tokenizer.setQuoteCharacter('"');
        tokenizer.setStrict(false);

        // Map CSV columns by header name instead of index
        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        flatFileItemReader.setLineMapper(lineMapper);
        flatFileItemReader.setLinesToSkip(1); // Skip header row
        flatFileItemReader.setSkippedLinesCallback(line -> {
            log.info(" READER: Skipped header line: {}", line);
        });

        log.info(" READER: CSV file reader configured successfully");
    }
    
    @Override
    public Customer read() throws Exception {
        if (flatFileItemReader == null) {
            initialize();
        }
        
        // Read next customer from CSV
        Customer customer = flatFileItemReader.read();
        
        if (customer != null) {
            readCount++;
            
            // Log progress for large files
            if (readCount % 5000 == 0) {
                log.info("READER: Read {} customers from CSV file", readCount);
            }
            
            log.info("READER: Read customer - Name: {}, Email: {}",
                        customer.getName(), customer.getEmail());
        } else {
            log.info("READER: Finished reading CSV file. Total customers read: {}", readCount);
        }
        
        return customer;
    }
}