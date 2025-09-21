//package com.assignment.customer_batch_processor;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//
//
//@SpringBootApplication
//public class CustomerBatchProcessor {
//
//	public static void main(String[] args) throws Exception {
//        var context = SpringApplication.run(CustomerBatchProcessor.class, args);
//
//        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
//        Job job = context.getBean("sampleJob", Job.class);
//
//        jobLauncher.run(job, new JobParameters());
//    }
//
//}

package com.assignment.customer_batch_processor;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class CustomerBatchProcessor {

    public static void main(String[] args) {
        SpringApplication.run(CustomerBatchProcessor.class, args);
    }
}