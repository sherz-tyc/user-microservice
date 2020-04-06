package com.faceit.demo.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class MessageProducer {
	private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerConfig.class);
	
	@Value("${kafka.topic}")
	private String USER_TOPIC;
	
	@Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
	
	/**
	 * Send event message to Kafka message queue with User Microservice Topic.
	 * @param message
	 */
	public void sendMessage(String message) {
		

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(USER_TOPIC, message);

        // Define call back functions to log success/failure
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
            	LOG.info("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata()
                    .offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
            	LOG.error("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            }
        });
    }

}
