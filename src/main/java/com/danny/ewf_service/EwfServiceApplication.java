package com.danny.ewf_service;

import com.danny.ewf_service.utils.imports.ImagesGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EwfServiceApplication implements CommandLineRunner {

	@Autowired
	private final ImagesGenerator imagesGenerator;
	private boolean hasRun = false; // Flag to


	public EwfServiceApplication(ImagesGenerator imagesGenerator) {
        this.imagesGenerator = imagesGenerator;
    }

    public static void main(String[] args) {
		SpringApplication.run(EwfServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (hasRun) {
			System.out.println("Task already executed. Skipping...");
			return; // Exit if already executed
		}

		String filePath = "src/main/resources/data/import.xlsx";
		imagesGenerator.generateImages(filePath);
		System.out.println("Application started successfully!");

		hasRun = true; // Mark as executed
	}

}