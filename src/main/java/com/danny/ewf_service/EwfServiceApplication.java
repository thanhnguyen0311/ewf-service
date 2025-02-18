package com.danny.ewf_service;

import com.danny.ewf_service.utils.imports.TitleGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EwfServiceApplication implements CommandLineRunner {

	@Autowired
	private final TitleGenerator titleGenerator;
	private boolean firstRun = true;


	public EwfServiceApplication(TitleGenerator titleGenerator) {
        this.titleGenerator = titleGenerator;
    }

    public static void main(String[] args) {
		SpringApplication.run(EwfServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		String filePath = "src/main/resources/data/ewfmain.csv";
		if (firstRun) {
			titleGenerator.generateLocalTitle();
			firstRun = false;
		}
		System.out.println("Application started successfully!");
	}
}