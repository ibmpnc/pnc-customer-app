package com.pnc.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = {"com.pnc.customer"})
@EnableRetry
@EnableCaching
@EnableScheduling
public class CustomerEventApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerEventApplication.class, args);
	}

}

