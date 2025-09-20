package com.assignment.customer_batch_processor.Controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class CustomerController {

	private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

	@PostMapping("/upload-xlsx")
	public ResponseEntity<String> handleExcelUpload(
			@RequestParam(value = "file", required = false) MultipartFile file) {

		if (file == null || file.isEmpty()) {
			return new ResponseEntity<>("File is not present. Please upload a file.", HttpStatus.BAD_REQUEST);
		}

		try {
			// Creates a unique temporary file with a .xlsx extension
			String uploadDir="src/main/resources/uploads";
			File directory=new File(uploadDir);
			if (!directory.exists()) {
				directory.mkdir();
			}
			 File savedFile = new File(directory, file.getOriginalFilename());

			// Writes the binary content of the uploaded file to the temporary file
			try (FileOutputStream fos = new FileOutputStream(savedFile)) {
				fos.write(file.getBytes());
			}

			return new ResponseEntity<>("File successfully uploaded to: " + savedFile.getAbsolutePath(), HttpStatus.OK);

		} catch (IOException e) {
			// Handle any exceptions that occur during file creation or writing
			throw new RuntimeException("Failed to save uploaded file.", e);
		} catch (Exception e) {
			logger.error("Failed to save file", e);
			return new ResponseEntity<>("Failed to save file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
