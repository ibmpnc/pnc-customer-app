package com.pnc.customer.processor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import org.apache.kafka.streams.kstream.SessionWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.pnc.customer.service.CustomerService;

@Service
public class CustomerEventProcessor {

	private static final Logger logger = LoggerFactory.getLogger(CustomerEventProcessor.class);

	@Value("${app.customer.topic}")
	private String incomingTopic;

	/*
	 * @KafkaListener(topics = "TestTopic", groupId = "group_id") public void
	 * consume(String message) {
	 * logger.info(String.format("$$$$ => Consumed message: %s", message)); }
	 */
	@Autowired
	private CustomerService customerService;
	private KafkaStreams streams;
	public static List<String> failedMessages =  new ArrayList<>();
	private static boolean isKafkaStreamInWait = false;
	
	@Scheduled(fixedDelay = 15000)
    public void scheduleFixedDelayTask() {
        
        if(isKafkaStreamInWait) {
        	isKafkaStreamInWait = false;
        	System.out.println("Notifying stream thread to start.");
        	startKafkaStreams();	
        }
    }

	//@Bean("customerKafkaProcessor")
	public Topology startKafkaStreams() {

		final Serde<String> stringSerde = Serdes.String();

		final StreamsBuilder builder = new StreamsBuilder();

		// Read the input Kafka topic into a KStream instance.
		final KStream<String, String> textLines = builder.stream(incomingTopic, Consumed.with(stringSerde, stringSerde));

		textLines
		.map((key, value) -> KeyValue.pair(value, value.toUpperCase()))
		//.filter((key, value) -> customerService.sendAccountDetails(value))
		.peek((key, value) -> peekAction(key, value))
		.to("RETRY_CUSTOMER");

		// spring reactive to separate REST POST call
		// Mockito test

		Topology topology = builder.build();
		streams = new KafkaStreams(topology, getStreamConfiguration());

		streams.cleanUp();
		streams.start();

		// Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
		Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
		return topology;
	}	

	public void stopKafkaStream() {
		
		isKafkaStreamInWait = true;
		try {
			Thread.sleep(8000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		streams.close();
		
	}

	private void peekAction(String key, String value) {
		logger.info("key::" + key + ", value::" + value);
		
		if(failedMessages.size() >= 3) {
			
			failedMessages.forEach((message) -> logger.error( " 3 Consecutive messages failed at EDGE API: " + message));
			
			failedMessages.clear();
			stopKafkaStream();
		}
	}

	private Properties getStreamConfiguration() {

		final String bootstrapServers = "localhost:9092";

		final Properties streamsConfiguration = new Properties();
		// Give the Streams application a unique name.  The name must be unique in the Kafka cluster
		// against which the application is run.
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "app-id-" + LocalDateTime.now());
		streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "client-id-" + LocalDateTime.now());
		// Where to find Kafka broker(s).
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 5000);
		// Specify default (de)serializers for record keys and for record values.
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		return streamsConfiguration;
	}
}
