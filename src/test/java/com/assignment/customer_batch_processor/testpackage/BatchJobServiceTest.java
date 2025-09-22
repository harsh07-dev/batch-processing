package com.assignment.customer_batch_processor.testpackage;


import com.assignment.customer_batch_processor.service.BatchJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.explore.JobExplorer;
import org.mockito.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BatchJobServiceTest {

    @InjectMocks
    private BatchJobService batchJobService;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job csvReadingJob;

    @Mock
    private JobExplorer jobExplorer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessCustomerFile_FileNotFound_ThrowsException() {
        String invalidFilePath = "nonexistent.csv";

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            batchJobService.processCustomerFile(invalidFilePath);
        });

        assertTrue(exception.getMessage().contains("Batch job execution failed"));
    }

    @Test
    void testProcessCustomerFile_ValidFile_JobExecutedSuccessfully() throws Exception {
        // Create a temporary file
        File tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit(); // cleanup

        // Mock JobExecution
        JobExecution mockExecution = mock(JobExecution.class);
        when(mockExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(mockExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);

        // Mock jobLauncher.run
        when(jobLauncher.run(eq(csvReadingJob), any(JobParameters.class))).thenReturn(mockExecution);

        JobExecution jobExecution = batchJobService.processCustomerFile(tempFile.getAbsolutePath());

        assertNotNull(jobExecution);
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        verify(jobLauncher, times(1)).run(eq(csvReadingJob), any(JobParameters.class));
    }

    @Test
    void testProcessCustomerFile_JobLauncherThrowsException() throws Exception {
        // Create a temporary file
        File tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();

        when(jobLauncher.run(eq(csvReadingJob), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Launcher failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            batchJobService.processCustomerFile(tempFile.getAbsolutePath());
        });

        assertTrue(exception.getMessage().contains("Batch job execution failed"));
    }
}
