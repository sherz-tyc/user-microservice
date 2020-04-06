package com.faceit.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

import com.faceit.demo.entity.User;
import com.faceit.demo.event.EventPublisher;
import com.faceit.demo.repo.UserRepository;

@TestInstance(Lifecycle.PER_CLASS)
@SpringBootTest
public class UserServiceTest {
	
	@Autowired
    private UserService userService;
	
	@Autowired UserRepository userRepo;
	
	@Autowired EventPublisher eventPublisher;
	
	@Value("${kafka.topic}")
	private String TOPIC_NAME;
	
	@Value("${kafka.port}")
	private String PORT;
	
	private static User userFrank;
	private static User userCesar;
	private static User userJohn;
	
	@ClassRule
    public EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, TOPIC_NAME);
	
	
	@BeforeAll
	public void setup() {
		
		ReflectionTestUtils.setField(userService, "userRepo", userRepo);
		ReflectionTestUtils.setField(userService, "eventPublisher", eventPublisher);
		
		embeddedKafka.kafkaPorts(Integer.valueOf(PORT));
		ContainerProperties containerProperties = new ContainerProperties(TOPIC_NAME);
		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(
				"sender", "false", embeddedKafka.getEmbeddedKafka());
		consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "user-service-test");
		BlockingQueue<ConsumerRecord<String, String>> consumerRecords = 
				new LinkedBlockingQueue<>();
		DefaultKafkaConsumerFactory<String, String> consumer = 
				new DefaultKafkaConsumerFactory<>(consumerProperties);
		KafkaMessageListenerContainer<String, String> container = 
				new KafkaMessageListenerContainer<>(consumer, containerProperties);
		container.setupMessageListener((MessageListener<String, String>) record -> {
			consumerRecords.add(record);
		});
		container.start();
		
		userFrank = new User("Frank", "Lampard", "Super Frank", "password", "f.lampard@cfc.com", "England");
		userCesar = new User("Cesar", "Azpilicueta", "Dave", "password", "c.azpi@cfc.com", "Spain");
		userJohn = new User("John", "Terry", "JT", "password", "j.terry@cfc.com", "England");
		userRepo.save(userFrank);
		userRepo.save(userCesar);
		userRepo.save(userJohn);
	}
	
	@Test
    public void whenFindAllUsers_thenAllUsersShouldBeReturned() {
		
		
	    Iterable<User> userIterable = userService.findAll();
	    List<User> userList = StreamSupport.stream(userIterable.spliterator(), false)
	    		    .collect(Collectors.toList());
	    int resultListSize = userList.size();
	    User resultSecondUser = userList.get(resultListSize -1);

	    assertEquals(3, resultListSize);
	    assertEquals(resultSecondUser.getFirstName(), userJohn.getFirstName());
	    assertEquals(resultSecondUser.getLastName(), userJohn.getLastName());
	    assertEquals(resultSecondUser.getNickName(), userJohn.getNickName());
	    assertEquals(resultSecondUser.getPassword(), userJohn.getPassword());
	    assertEquals(resultSecondUser.getEmail(), userJohn.getEmail());
	    assertEquals(resultSecondUser.getCountry(), userJohn.getCountry());
    }
	
	@Test
    public void givenValiduserId_whenFindUserById_thenUserWithIdShouldBeReturned() {
		
	    User resultUser = userService.findById(2l);
	    
	    assertEquals(resultUser.getFirstName(), userCesar.getFirstName());
	    assertEquals(resultUser.getLastName(), userCesar.getLastName());
	    assertEquals(resultUser.getNickName(), userCesar.getNickName());
	    assertEquals(resultUser.getPassword(), userCesar.getPassword());
	    assertEquals(resultUser.getEmail(), userCesar.getEmail());
	    assertEquals(resultUser.getCountry(), userCesar.getCountry());
    }
	
	/*
	 * TODO: More unit tests to follow.
	 * 
	 * Comprehensive tests are in UserMicroserviceIT, which has Integration Tests that
	 * covers the UserService.
	 */

}
