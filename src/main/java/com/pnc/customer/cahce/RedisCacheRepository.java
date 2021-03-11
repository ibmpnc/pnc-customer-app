package com.pnc.customer.cahce;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisCacheRepository {

	private static final Logger logger = LoggerFactory.getLogger(RedisCacheRepository.class);
	private HashOperations hashOperations;

    private RedisTemplate redisTemplate;

    public RedisCacheRepository(RedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
        this.hashOperations = this.redisTemplate.opsForHash();
    }

    public void save(String timeStamp){
    	logger.info("timestamp:: " + timeStamp);
        hashOperations.put("EDGE_FAILED_TIME", "failedTime", timeStamp);
        logger.info("readtime:: " + (String) hashOperations.get("EDGE_FAILED_TIME", "failedTime"));
    }
    
    public List findAll(){
        return hashOperations.values("EDGE_FAILED_TIME");
    }

    public String findById(){
        return (String) hashOperations.get("EDGE_FAILED_TIME", "failedTime");
    }

}
