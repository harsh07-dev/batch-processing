package com.assignment.customer_batch_processor.writer;

import com.assignment.customer_batch_processor.Customer_Entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * WRITER COMPONENT
 * 
 * Responsibility:
 * 1. Receive processed Customer objects from Processor
 * 2. Save customers to database in chunks
 * 3. Handle database transactions
 * 4. Manage duplicate records
 * 5. Log writing progress and statistics
 * 6. Handle database errors gracefully
 */
@Component
public class CustomerItemWriter implements ItemWriter<Customer> {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerItemWriter.class);
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private int totalWritten = 0;
    private int successfulWrites = 0;
    private int failedWrites = 0;
    
    @Override
    @Transactional
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        
        List<? extends Customer> customers = chunk.getItems();
        int chunkSize = customers.size();
        
        logger.info("💾 WRITER: Writing chunk of {} customers to database", chunkSize);
        
        int successCount = 0;
        int failCount = 0;
        
        for (Customer customer : customers) {
            try {
                // STEP 1: Set audit fields
                setAuditFields(customer);
                
                // STEP 2: Check for duplicates
                if (isDuplicateCustomer(customer)) {
                    logger.warn("⚠️ WRITER: Duplicate customer found - Email: {}", customer.getEmail());
                    failCount++;
                    continue;
                }
                
                // STEP 3: Save to database
                saveCustomer(customer);
                successCount++;
                
                logger.debug("✅ WRITER: Saved customer - ID: {}, Name: {}, Email: {}", 
                           customer.getId(), customer.getName(), customer.getEmail());
                
            } catch (Exception e) {
                failCount++;
                logger.error("❌ WRITER: Failed to save customer {}: {}", 
                           customer.getName(), e.getMessage());
                
                // Don't throw exception - just log and continue with next record
                // This ensures one bad record doesn't fail the entire chunk
            }
        }
        
        // STEP 4: Flush changes to database
        try {
            entityManager.flush();
            entityManager.clear(); // Clear persistence context to free memory
        } catch (Exception e) {
            logger.error("❌ WRITER: Error flushing entity manager: {}", e.getMessage());
            throw e;
        }
        
        // STEP 5: Update statistics
        updateStatistics(successCount, failCount);
        
        // STEP 6: Log chunk results
        logChunkResults(chunkSize, successCount, failCount);
        
        // STEP 7: Log overall progress
        logOverallProgress();
    }
    
    /**
     * STEP 1: Set audit fields for customer
     */
    private void setAuditFields(Customer customer) {
        LocalDateTime now = LocalDateTime.now();
        customer.setCreatedDate(now);
        customer.setUpdatedDate(null); // New record, so no update date
    }
    
    /**
     * STEP 2: Check if customer already exists (by email)
     */
    private boolean isDuplicateCustomer(Customer customer) {
        try {
            Long count = entityManager.createQuery(
                "SELECT COUNT(c) FROM Customer c WHERE c.email = :email", Long.class)
                .setParameter("email", customer.getEmail())
                .getSingleResult();
            
            return count > 0;
            
        } catch (Exception e) {
            logger.warn("⚠️ WRITER: Error checking for duplicate: {}", e.getMessage());
            return false; // If check fails, proceed with save
        }
    }
    
    /**
     * STEP 3: Save customer to database
     */
    private void saveCustomer(Customer customer) {
        entityManager.persist(customer);
    }
    
    /**
     * STEP 5: Update writing statistics
     */
    private void updateStatistics(int successCount, int failCount) {
        totalWritten += (successCount + failCount);
        successfulWrites += successCount;
        failedWrites += failCount;
    }
    
    /**
     * STEP 6: Log chunk processing results
     */
    private void logChunkResults(int chunkSize, int successCount, int failCount) {
        if (failCount > 0) {
            logger.warn("📊 WRITER: Chunk processed - Total: {}, Saved: {}, Failed: {}", 
                       chunkSize, successCount, failCount);
        } else {
            logger.info("📊 WRITER: Chunk processed successfully - Total: {}, Saved: {}", 
                       chunkSize, successCount);
        }
    }
    
    /**
     * STEP 7: Log overall progress
     */
    private void logOverallProgress() {
        if (totalWritten % 5000 == 0 && totalWritten > 0) {
            double successRate = (double) successfulWrites / totalWritten * 100;
            logger.info("📈 WRITER: Overall Progress - Total: {}, Success: {} ({:.1f}%), Failed: {}", 
                       totalWritten, successfulWrites, successRate, failedWrites);
        }
    }
    
    /**
     * Get writing statistics
     */
    public WritingStats getStats() {
        return new WritingStats(totalWritten, successfulWrites, failedWrites);
    }
    
    /**
     * Reset writer statistics
     */
    public void reset() {
        totalWritten = 0;
        successfulWrites = 0;
        failedWrites = 0;
    }
    
    /**
     * Batch write completion callback
     */
    public void onWriteCompletion() {
        logger.info("🏁 WRITER: Batch writing completed!");
        logger.info("📋 WRITER: Final Statistics - Total: {}, Successful: {}, Failed: {}", 
                   totalWritten, successfulWrites, failedWrites);
        
        if (failedWrites > 0) {
            double failureRate = (double) failedWrites / totalWritten * 100;
            logger.warn("⚠️ WRITER: Failure rate: {:.2f}%", failureRate);
        }
    }
    
    /**
     * Writing statistics holder
     */
    public static class WritingStats {
        private final int total;
        private final int successful;
        private final int failed;
        
        public WritingStats(int total, int successful, int failed) {
            this.total = total;
            this.successful = successful;
            this.failed = failed;
        }
        
        public int getTotal() { return total; }
        public int getSuccessful() { return successful; }
        public int getFailed() { return failed; }
        
        public double getSuccessRate() {
            return total > 0 ? (double) successful / total * 100 : 0;
        }
        
        @Override
        public String toString() {
            return String.format("WritingStats{total=%d, successful=%d, failed=%d, successRate=%.1f%%}", 
                               total, successful, failed, getSuccessRate());
        }
    }
}