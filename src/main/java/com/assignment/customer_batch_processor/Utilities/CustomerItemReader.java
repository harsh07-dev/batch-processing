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
@Slf4j
public class CustomerItemReader {

    public static FlatFileItemReader<Customer> customerFlatFileItemReader(String filePath) {

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

}