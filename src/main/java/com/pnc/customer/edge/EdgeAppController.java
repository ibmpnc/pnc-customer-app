package com.pnc.customer.edge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class EdgeAppController {
	private static final Logger logger = LoggerFactory.getLogger(EdgeAppController.class);
	
	@RequestMapping(method = RequestMethod.POST, value="/data")
	public String saveAccountData(@RequestBody String request) throws Exception{
		
		if("test".equalsIgnoreCase(request)){
			throw new Exception("Invalid request");
		}
		logger.info("request received in the POST API :: " + request);
		return "Success";
	}
	
	@GetMapping("/")
	public String readData() {
		return "Hello!!";
	}
	
	

}
