package com.test.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomerService {
	private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
	
	
	@Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 5000))
	public boolean sendAccountDetails(String value)
	{
	    final String uri = "http://localhost:9090/data";
	    RestTemplate restTemplate = new RestTemplate();
	 
	    try {
	    	String result = restTemplate.postForObject(uri, value, String.class);
		    logger.info("Rest API call response :: " + result);
	    } catch(Exception ex) {
	    	logger.error("Exception from REST API call");
	    	throw ex;
	    }
	    
	    return false;
	}
	
	@Recover
	public boolean recovderSendAccountDetails(Exception ex, String value) {
		
		return true;
	}

}
