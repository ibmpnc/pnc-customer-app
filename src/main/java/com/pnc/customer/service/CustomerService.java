package com.pnc.customer.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.pnc.customer.cahce.RedisCacheRepository;

@Component
public class CustomerService {
	private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
	
	@Autowired
	private RedisCacheRepository redisCacheRepository;
	
	@Retryable(value = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 2000))
	public boolean sendAccountDetails(String value)
	{
	    final String uri = "http://localhost:9095/data";
	    RestTemplate restTemplate = new RestTemplate();
	 
	    try {
	    	logger.info("Before EDGE API call ");
	    	String result = restTemplate.postForObject(uri, value, String.class);
		    logger.info("EDGE API call response :: " + result);
	    } catch(Exception ex) {
	    	logger.error("Exception from EDGE API call");
	    	throw ex;
	    }
	    
	    return false;
	}
	
	@Recover
	public boolean recovderSendAccountDetails(Exception ex, String value) {
		//redisCacheRepository.save(LocalDateTime.now().toString());
    	//logger.info("Last timestamp stored in Redis cache " + redisCacheRepository.findById());
		return true;
	}

}
