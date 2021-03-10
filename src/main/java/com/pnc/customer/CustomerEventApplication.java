package com.pnc.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;


@SpringBootApplication(scanBasePackages = {"com.pnc.customer", "com.test.kafka"})
@EnableRetry
public class CustomerEventApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerEventApplication.class, args);
	}

}

