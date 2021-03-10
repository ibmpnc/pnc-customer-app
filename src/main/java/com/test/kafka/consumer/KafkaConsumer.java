package com.test.kafka.consumer;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.test.kafka.service.CustomerService;

@Service
public class KafkaConsumer {
	private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);


	/*
	 * @KafkaListener(topics = "TestTopic", groupId = "group_id") public void
	 * consume(String message) {
	 * logger.info(String.format("$$$$ => Consumed message: %s", message)); }
	 */
	@Autowired
	private CustomerService customerService;
	
	@Bean("kafkaConsumerProcessor")
	public Topology startKafkaStreams() {
		
	    final Serde<String> stringSerde = Serdes.String();
 
	    final StreamsBuilder builder = new StreamsBuilder();

	    // Read the input Kafka topic into a KStream instance.
	    final KStream<String, String> textLines = builder.stream("ACCOUNT", Consumed.with(stringSerde, stringSerde));
	    
	    textLines
	    	.map((key, value) -> KeyValue.pair(value, value.toUpperCase()))
	    	.filter((key, value) -> customerService.sendAccountDetails(value))
	    	.peek((key, value) -> logger.info("key::" + key + ", value::" + value))
	    	.to("PUBLISH");
	    
	    // spring reactive to separate REST POST call
	    // Mockito test
	    
	    Topology topology = builder.build();
	    final KafkaStreams streams = new KafkaStreams(topology, getStreamConfiguration());
	    
	    streams.cleanUp();
	    streams.start();

	    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
	    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
	    return topology;
	}
	
	private Properties getStreamConfiguration() {

		final String bootstrapServers = "localhost:9092";
		
		final Properties streamsConfiguration = new Properties();
	    // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
	    // against which the application is run.
	    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "map-function-lambda-example");
	    streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "map-function-lambda-example-client");
	    // Where to find Kafka broker(s).
	    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
	    // Specify default (de)serializers for record keys and for record values.
	    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
	    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
	    return streamsConfiguration;
	}
	
	
	
}
