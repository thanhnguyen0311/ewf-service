package com.danny.ewf_service;

import com.danny.ewf_service.utils.SQLExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EwfServiceApplication implements CommandLineRunner {

	@Autowired
	private SQLExecutor sqlExecutor;

	public static void main(String[] args) {
		SpringApplication.run(EwfServiceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Starting application...");
		// Automatically execute all SQL scripts at startup
		sqlExecutor.executeAllSQLScripts();
		System.out.println("Application started successfully!");
	}
}