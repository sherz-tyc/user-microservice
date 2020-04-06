package com.faceit.demo.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.faceit.demo.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component("eventPublisher")
public class EventPublisher {
	private static final Logger LOG = LoggerFactory.getLogger(EventPublisher.class);
	
	@Autowired
	MessageProducer messageProducer;
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	public void publishEvent(Events event, User user) {
		
		try {
			// Attempt to serialise User entity to Json format for message.
			String userRepresentation = mapper.writeValueAsString(user);
			this.publishEvent(event, userRepresentation);
			
		} catch (JsonProcessingException jex) {
			LOG.warn(String.format("Error processing User entity to JSON format, "
					+ "representing user with ID instead = %d", user.getId()));
			this.publishEvent(event, String.valueOf(user.getId()));
		}
    }
	
	public void publishEvent(Events event, String id){
		ObjectNode node = mapper.getNodeFactory().objectNode();
	    node.put("event", event.stringVal);
	    node.put("user", id);
	    messageProducer.sendMessage(node.toString());
        LOG.info(String.format("Event is sent to Message Queue: %s", node.toString()));
    }

}
