package com.assignment.customer_batch_processor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Encryption Service for securing sensitive data (Aadhaar & PAN numbers)
 * Uses AES encryption algorithm
 */
@Service
@Slf4j
public class EncryptionService {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    
    @Value("${app.encryption.secret-key:MySecretKey12345}")  // 16 characters for AES-128
    private String secretKey;
    
    /**
     * Get AES secret key
     */
    private SecretKey getSecretKey() {
        // Ensure key is exactly 16 bytes for AES-128
        String key = secretKey;
        log.info("Encryption key {} ", key);
        if (key.length() < 16) {
            key = key + "0".repeat(16 - key.length());
        } else if (key.length() > 16) {
            key = key.substring(0, 16);
        }
        return new SecretKeySpec(key.getBytes(), ALGORITHM);
    }
    
    /**
     * Encrypts the given plain text using AES encryption
     * @param plainText The text to encrypt (Aadhaar or PAN number)
     * @return Base64 encoded encrypted string
     */
    public String encrypt(String plainText) {
        try {
            if (plainText == null || plainText.trim().isEmpty()) {
                return plainText;
            }
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            String encrypted = Base64.getEncoder().encodeToString(encryptedBytes);
            
            log.debug("ENCRYPTION: Successfully encrypted data of length: {}", plainText.length());
            return encrypted;
            
        } catch (Exception e) {
            log.error("ENCRYPTION: Error encrypting data: {}", e.getMessage());
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
//    /**
//     * Decrypts the given encrypted text using AES decryption
//     * @param encryptedText Base64 encoded encrypted string
//     * @return Decrypted plain text
//     */
//    public String decrypt(String encryptedText) {
//        try {
//            if (encryptedText == null || encryptedText.trim().isEmpty()) {
//                return encryptedText;
//            }
//
//            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
//
//            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
//            String decrypted = new String(decryptedBytes);
//
//            log.debug("DECRYPTION: Successfully decrypted data");
//            return decrypted;
//
//        } catch (Exception e) {
//            log.error("DECRYPTION: Error decrypting data: {}", e.getMessage());
//            throw new RuntimeException("Decryption failed", e);
//        }
//    }
    
//    /**
//     * Validate if the encryption service is working properly
//     * @return true if encryption/decryption works correctly
//     */
//    public boolean validateEncryptionService() {
//        try {
//            String testData = "TEST123";
//            String encrypted = encrypt(testData);
//            String decrypted = decrypt(encrypted);
//
//            boolean isValid = testData.equals(decrypted);
//            log.info("VALIDATION: Encryption service test - {}", isValid ? "PASSED" : "FAILED");
//            return isValid;
//
//        } catch (Exception e) {
//            log.error("VALIDATION: Encryption service validation failed: {}", e.getMessage());
//            return false;
//        }
//    }
}