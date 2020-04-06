package com.faceit.demo.event;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import com.faceit.demo.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;

/**
 * This is the Integration Test that covers the Kafka Message Producer, which
 * is the EventPubllisher class in the User Microservice.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
@DirtiesContext
@TestInstance(Lifecycle.PER_CLASS)
public class EventPublisherIT {
	private static final Logger LOG = LoggerFactory.getLogger(EventPublisherIT.class);
	
	@Value("${kafka.topic}")
	private String TOPIC;
	
	@Value("${kafka.port}")
	private String PORT;
	
	@LocalServerPort
	private int port;
	
	private static final String BASE_USER_ROUTE = "/user";
	
	private static final String CREATE_USER_ENDPOINT = "/create";
	private static final String UPDATE_USER_ENDPOINT = "/update";
	private static final String DELETE_USER_ENDPOINT = "/remove/";
	
	private KafkaMessageListenerContainer<String, String> container;
    private BlockingQueue<ConsumerRecord<String, String>> consumerRecords;

    @ClassRule
    public EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, TOPIC);
	
	@BeforeAll
    public void setup() {
		RestAssured.defaultParser = Parser.JSON;
		RestAssured.port = port;
		User userLionel = new User("Leo", "Messi", "GOAT", "password", "l.messi@bfc.com", "Argentina");
		given().contentType(ContentType.JSON).body(userLionel).post(BASE_USER_ROUTE + CREATE_USER_ENDPOINT);
		
		consumerRecords = new LinkedBlockingQueue<>();
		embeddedKafka.kafkaPorts(Integer.valueOf(PORT));
		ContainerProperties containerProperties = new ContainerProperties(TOPIC);
		
		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(
				"sender", "false", embeddedKafka.getEmbeddedKafka());
		consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "event-publisher-it");
		consumerProperties.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 1000);
		
		DefaultKafkaConsumerFactory<String, String> consumer = 
				new DefaultKafkaConsumerFactory<>(consumerProperties);
		
		container = new KafkaMessageListenerContainer<>(consumer, containerProperties);
		container.setupMessageListener((MessageListener<String, String>) record -> {
			LOG.debug("Listened message='{}'", record.toString());
			consumerRecords.add(record);
		});
		container.start();
		ContainerTestUtils.waitForAssignment(container, 1);
		
		try {
			// confirm that when sample user is created, event message is added to Queue.
			ConsumerRecord<String, String> received = consumerRecords.poll(3, TimeUnit.SECONDS);
			assertThat(received.topic(), is(TOPIC));
		} catch (InterruptedException e) {
			assertFalse(true);
		}

    }
	
	@AfterAll
	public void teardown() {
		container.stop();
	}
    
	@Test
	@Order(1)
	@Transactional
	public void givenValidUser_whenUserCreatedSuccessfully_thenUserCreateMessageIsSentCorrectly() {
		RestAssured.defaultParser = Parser.JSON;
		final String FIRSTNAME = "Cristiano", LASTNAME = "Ronaldo", NICKNAME = "R7",
				PASSWORD = "password", EMAIL = "c.ronaldo@jfc.com", COUNTRY = "Portugal";
		User user = new User(FIRSTNAME, LASTNAME, NICKNAME, PASSWORD, EMAIL, COUNTRY);
		given().contentType(ContentType.JSON).body(user)
				.when().post(BASE_USER_ROUTE + CREATE_USER_ENDPOINT).then()
				.assertThat().statusCode(equalTo(HttpStatus.CREATED.value()));
		
		try {
			ConsumerRecord<String, String> received = consumerRecords.poll(3, TimeUnit.SECONDS);
			
			JSONObject jsonObject = new JSONObject(received.value());
			String eventString = (String) jsonObject.get("event");
			String userString = (String) jsonObject.get("user");
			User createdUser = new ObjectMapper().readValue(userString, User.class);

			assertThat(received.topic(), is(TOPIC));
			assertThat(eventString, is(Events.USER_CREATED.stringVal));
			assertThat(createdUser.getId(), is(greaterThan(0l)));
			assertThat(createdUser.getFirstName(), is(equalTo(FIRSTNAME)));
			assertThat(createdUser.getLastName(), is(equalTo(LASTNAME)));
			assertThat(createdUser.getNickName(), is(equalTo(NICKNAME)));
			assertThat(createdUser.getPassword(), is(equalTo(PASSWORD)));
			assertThat(createdUser.getEmail(), is(equalTo(EMAIL)));
			assertThat(createdUser.getCountry(), is(equalTo(COUNTRY)));
			
		} catch (InterruptedException e) {
			LOG.error("Error Consuming Messages: " + e);
			assertFalse(true);
		} catch (JSONException e) {
			LOG.error("Error Processing JSON String: " + e);
			assertFalse(true);
		} catch (JsonMappingException e) {
			LOG.error("Error Mapping JSON String to User: " + e);
			assertFalse(true);
		} catch (JsonProcessingException e) {
			LOG.error("Error Processing JSON String: " + e);
			assertFalse(true);
		}
        
	}
	
	@Test
	@Order(2)
	public void givenInvalidUserWithId_whenCreateUserailed_thenNoEventMessageIsSent() {
		RestAssured.defaultParser = Parser.JSON;
		User user = new User(3l, "Juan", "Mata", "JM", "password", "j.mata@cfc.com", "Spain");
		given().contentType(ContentType.JSON).body(user)
				.when().post(BASE_USER_ROUTE + CREATE_USER_ENDPOINT).then()
				.assertThat().statusCode(equalTo(HttpStatus.NOT_ACCEPTABLE.value()));
		
			
		assertThat(consumerRecords.size(), is(0));
	}
	
	@Test
	@Order(3)
	public void givenValidUserWithId_whenUpdatedUserSuccessfully_thenUserUpdatedMessageIsSentCorrectly() {
		RestAssured.defaultParser = Parser.JSON;
		final String FIRSTNAME = "Sadio", LASTNAME = "Mane", NICKNAME = "SM",
				PASSWORD = "password", EMAIL = "s.mane@lfc.com", COUNTRY = "Senegal";
		User user = new User(1l, "Sadio", "Mane", "SM", "password", EMAIL, "Senegal");
		given().contentType(ContentType.JSON).body(user)
				.when().put(BASE_USER_ROUTE + UPDATE_USER_ENDPOINT).then()
				.assertThat().statusCode(equalTo(HttpStatus.OK.value()));
		
		try {
			ConsumerRecord<String, String> received = consumerRecords.poll(3, TimeUnit.SECONDS);
			
			JSONObject jsonObject = new JSONObject(received.value());
			String eventString = (String) jsonObject.get("event");
			String userString = (String) jsonObject.get("user");
			User createdUser = new ObjectMapper().readValue(userString, User.class);

			assertThat(received.topic(), is(TOPIC));
			assertThat(eventString, is(Events.USER_UPDATED.stringVal));
			assertThat(createdUser.getId(), is(greaterThan(0l)));
			assertThat(createdUser.getFirstName(), is(equalTo(FIRSTNAME)));
			assertThat(createdUser.getLastName(), is(equalTo(LASTNAME)));
			assertThat(createdUser.getNickName(), is(equalTo(NICKNAME)));
			assertThat(createdUser.getPassword(), is(equalTo(PASSWORD)));
			assertThat(createdUser.getEmail(), is(equalTo(EMAIL)));
			assertThat(createdUser.getCountry(), is(equalTo(COUNTRY)));
			
		} catch (InterruptedException e) {
			LOG.error("Error Consuming Messages: " + e);
			assertFalse(true);
		} catch (JSONException e) {
			LOG.error("Error Processing JSON String: " + e);
			assertFalse(true);
		} catch (JsonMappingException e) {
			LOG.error("Error Mapping JSON String to User: " + e);
			assertFalse(true);
		} catch (JsonProcessingException e) {
			LOG.error("Error Processing JSON String: " + e);
			assertFalse(true);
		}
        
	}
	
	@Test
	@Order(4)
	public void givenUserWithInvalidId_whenUpdateUserFailed_thenNoEventMessageIsSent() {
		RestAssured.defaultParser = Parser.JSON;
		User user = new User(99l, "Sadio", "Mane", "SM", "password", "s.mane@lfc.com", "Senegal");
		given().contentType(ContentType.JSON).body(user)
				.when().put(BASE_USER_ROUTE + UPDATE_USER_ENDPOINT).then()
				.assertThat().statusCode(equalTo(HttpStatus.NOT_FOUND.value()));
		assertThat(consumerRecords.size(), is(0));
	}
	
	@Test
	@Order(5)
	public void givenValidUserId_whenRemovedUserSuccessfully_thenUserDeletedMessageIsSentCorrectly() {
		final String USER_ID = "1";
		RestAssured.defaultParser = Parser.JSON;
		given().contentType(ContentType.JSON)
				.when().delete(BASE_USER_ROUTE + DELETE_USER_ENDPOINT + USER_ID).then()
				.assertThat().statusCode(equalTo(HttpStatus.ACCEPTED.value()));
		
		try {
			ConsumerRecord<String, String> received = consumerRecords.poll(3, TimeUnit.SECONDS);
			
			JSONObject jsonObject = new JSONObject(received.value());
			String eventString = (String) jsonObject.get("event");
			String userIdString = (String) jsonObject.get("user");

			assertThat(received.topic(), is(TOPIC));
			assertThat(eventString, is(Events.USER_DELETED.stringVal));
			assertThat(userIdString, is(USER_ID));
			
		} catch (InterruptedException e) {
			LOG.error("Error Consuming Messages: " + e);
			assertFalse(true);
		} catch (JSONException e) {
			LOG.error("Error Processing JSON String: " + e);
			assertFalse(true);
		}
	}
	
	@Test
	@Order(6)
	public void givenInvalidUserId_whenRemoveUserailed_thenNoEventMessageIsSent() {
		final String USER_ID = "99";
		RestAssured.defaultParser = Parser.JSON;
		given().contentType(ContentType.JSON)
				.when().delete(BASE_USER_ROUTE + DELETE_USER_ENDPOINT + USER_ID).then()
				.assertThat().statusCode(equalTo(HttpStatus.NOT_FOUND.value()));
		assertThat(consumerRecords.size(), is(0));
	}

}
