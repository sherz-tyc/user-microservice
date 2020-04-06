package com.faceit.demo.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Configuration for Producer of event messages
 */
@Configuration
public class KafkaProducerConfig {
	
	@Value("${kafka.topic}")
	private String USER_TOPIC;
	
	@Value("${kafka.address}")
	private String KAFKA_ADDRESS;
	
	@Value("${kafka.serializer}")
	private String SERIALIZER_CLASS;
	
	@Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_ADDRESS);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, SERIALIZER_CLASS);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SERIALIZER_CLASS);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
	
	@Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
