package com.assignment.customer_batch_processor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


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
        log.debug("Encryption key {} ", key);
        if (key.length() < 16) {
            key = key + "0".repeat(16 - key.length());
        } else if (key.length() > 16) {
            key = key.substring(0, 16);

        }
        return new SecretKeySpec(key.getBytes(), ALGORITHM);
    }

    /**
     * Encrypts the given plain text using AES encryption
     *
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
}