package com.danny.ewf_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.danny.ewf_service.converter")
@SpringBootApplication
public class EwfServiceApplication implements CommandLineRunner {



    public static void main(String[] args) {
		SpringApplication.run(EwfServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Application started successfully!");
	}
}