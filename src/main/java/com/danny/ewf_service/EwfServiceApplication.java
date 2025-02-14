package com.danny.ewf_service;

import com.danny.ewf_service.utils.ImageCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EwfServiceApplication implements CommandLineRunner {

	@Autowired
	private final ImageCheck imageCheck;


	public EwfServiceApplication(ImageCheck imageCheck) {
        this.imageCheck = imageCheck;
    }

    public static void main(String[] args) {
		SpringApplication.run(EwfServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Application started successfully!");
	}
}