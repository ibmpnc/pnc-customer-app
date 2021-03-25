package com.pnc.customer.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.pnc.customer.cahce.RedisCacheRepository;
import com.pnc.customer.consumer.api.CustomerConsumerApi;

@Component
public class CustomerService {
	private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
	
	@Retryable(value = { Exception.class }, maxAttemptsExpression = "${app.customer.retry.maxattempt}", 
			backoff = @Backoff(delayExpression = "${app.customer.retry.delay}"))
	public boolean sendAccountDetails(ConsumerRecord<String, String> record)
	{
	    final String uri = "http://localhost:9095/data";
	    RestTemplate restTemplate = new RestTemplate();
	 
	    try {
	    	//logger.info("Before EDGE API call ");
	    	String result = restTemplate.postForObject(uri, record.value(), String.class);
	    	CustomerConsumerApi.failedMessages.clear();
		    //logger.info("EDGE API call response :: " + result);
	    } catch(Exception ex) {	    	
	    	//logger.error("Exception from EDGE API call");
	    	throw ex;
	    }
	    
	    return false;
	}
	
	@Recover
	public boolean recovderSendAccountDetails(Exception ex, ConsumerRecord<String, String> record) {
		//redisCacheRepository.save(LocalDateTime.now().toString());
    	//logger.info("Last timestamp stored in Redis cache " + redisCacheRepository.findById());
		CustomerConsumerApi.failedMessages.add(record);
		return true;
	}

}
