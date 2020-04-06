package com.faceit.demo;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.ClassRule;
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
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.faceit.demo.entity.User;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;

/**
 * This is the main Integration Test that covers the functions and cycle of
 * the User MicroService, from Controller, to Service, to Repository.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
@DirtiesContext
@TestInstance(Lifecycle.PER_CLASS)
public class UserMicroserviceIT {
	private static final Logger LOG = LoggerFactory.getLogger(UserMicroserviceIT.class);

	
	@Value("${kafka.topic}")
	private String TOPIC_NAME;
	
	@Value("${kafka.port}")
	private String PORT;
	
	@LocalServerPort
	private int port;
	
	private static final String BASE_USER_ROUTE = "/user";
	
	private static final String LIST_USERS_ENDPOINT = "/listAll";
	private static final String CREATE_USER_ENDPOINT = "/create";
	private static final String READ_USER_ENDPOINT = "/find/id/";
	private static final String UPDATE_USER_ENDPOINT = "/update";
	private static final String DELETE_USER_ENDPOINT = "/remove/";
	private static final String SEARCH_USER_ENDPOINT = "/search?criteria=";
	
    
    @ClassRule
    public EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, TOPIC_NAME);
	
	@BeforeAll
    public void seedData() {
		
		embeddedKafka.kafkaPorts(Integer.valueOf(PORT));
		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(
				"sender", "false", embeddedKafka.getEmbeddedKafka());
		consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "user-microservice-it");
		
		DefaultKafkaConsumerFactory<String, String> consumerFactory = 
				new DefaultKafkaConsumerFactory<>(consumerProperties);
		ContainerProperties containerProperties = 
				new ContainerProperties(TOPIC_NAME);
		KafkaMessageListenerContainer<String, String> container = 
				new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
		
		container.setupMessageListener((MessageListener<String, String>) record -> {
			LOG.debug("Listened message='{}'", record.toString());
		});
		container.start();
		
		RestAssured.defaultParser = Parser.JSON;
		RestAssured.port = port;
    	User userJoe = new User("Joe", "Cole", "JCole", "password", "j.cole@cfc.com", "England");
    	User userDavid = new User("David", "Beckham", "Becks", "password", "d.beckham@mfc.com", "England");
    	User userLionel = new User("Lionel", "Messi", "GOAT", "password", "l.messi@bfc.com", "Argentina");
    	User userMo = new User("Mo", "Salah", "Mo", "password", "m.salah@lfc.com", "Egypt");
    	given().contentType(ContentType.JSON).body(userJoe).post(BASE_USER_ROUTE + CREATE_USER_ENDPOINT);
    	given().contentType(ContentType.JSON).body(userDavid).post(BASE_USER_ROUTE + CREATE_USER_ENDPOINT);
    	given().contentType(ContentType.JSON).body(userLionel).post(BASE_USER_ROUTE + CREATE_USER_ENDPOINT);
    	given().contentType(ContentType.JSON).body(userMo).post(BASE_USER_ROUTE + CREATE_USER_ENDPOINT);
    	
    }
	
	@Test
	@Order(1)
	public void givenValidUser_whenCreateUser_thenReturnStatusCode200() {
		RestAssured.defaultParser = Parser.JSON;
		User user = new User("Cristiano", "Ronaldo", "R7", "password", "c.ronaldo@jfc.com", "Portugal");
		given().contentType(ContentType.JSON).body(user)
				.when().post(BASE_USER_ROUTE + CREATE_USER_ENDPOINT).then()
				.assertThat().statusCode(equalTo(HttpStatus.CREATED.value()));
	}
	
	@Test
	@Order(2)
	public void givenValidUser_whenCreateUser_thenReturnUserWithNewId() {
		RestAssured.defaultParser = Parser.JSON;
		User user = new User("Cesar", "Azpilicueta", "Dave", "password", "c.azpi@cfc.com", "Spain");
		given().contentType(ContentType.JSON).body(user)
				.when().post(BASE_USER_ROUTE + CREATE_USER_ENDPOINT).then()
				.assertThat().body("$", hasKey("id"));
	}
	
	@Test
	@Order(3)
	public void givenInvalidUserWithId_whenCreateUser_thenReturnStatusCode406() {
		RestAssured.defaultParser = Parser.JSON;
		User user = new User(3l, "Juan", "Mata", "JM", "password", "j.mata@cfc.com", "Spain");
		given().contentType(ContentType.JSON).body(user)
				.when().post(BASE_USER_ROUTE + CREATE_USER_ENDPOINT).then()
				.assertThat().statusCode(equalTo(HttpStatus.NOT_ACCEPTABLE.value()));
	}
	
	@Test
	@Order(4)
	public void givenUsersOnDatabase_whenListAllUsers_thenReturnAllUsers() {
		RestAssured.defaultParser = Parser.JSON;
		given().contentType(ContentType.JSON)
		.when().get(BASE_USER_ROUTE + LIST_USERS_ENDPOINT).then()
				.assertThat().body("$", hasSize(6));
	}
	
	@Test
	@Order(5)
	public void givenValidUserId_whenSearchForUserById_thenReturnUser() {
		final String USER_ID = "2";
		RestAssured.defaultParser = Parser.JSON;
		given().contentType(ContentType.JSON)
				.when().get(BASE_USER_ROUTE + READ_USER_ENDPOINT + USER_ID).then()
				.assertThat().body("", hasKey("id"))
				.assertThat().body("id", is(2));
	}
	
	@Test
	@Order(6)
	public void givenInvalidUserId_whenSearchForUserById_thenReturnStatusCode404() {
		final String USER_ID = "99";
		RestAssured.defaultParser = Parser.JSON;
		given().contentType(ContentType.JSON)
				.when().get(BASE_USER_ROUTE + READ_USER_ENDPOINT + USER_ID).then()
				.assertThat().statusCode(equalTo(HttpStatus.NOT_FOUND.value()));
	}
	
	
	@Test
	@Order(7)
	public void givenValidUserWithId_whenUpdateUser_thenReturnStatusCode200AndUpdatedField() {
		RestAssured.defaultParser = Parser.JSON;
		final String email = "s.mane@lfc.com";
		User user = new User(4l, "Sadio", "Mane", "SM", "password", email, "Senegal");
		given().contentType(ContentType.JSON).body(user)
				.when().put(BASE_USER_ROUTE + UPDATE_USER_ENDPOINT).then()
				.assertThat().statusCode(equalTo(HttpStatus.OK.value()))
				.assertThat().body("email", is(email))
				.assertThat().body("id", is(4));
	}
	
	@Test
	@Order(8)
	public void givenUserWithInvalidId_whenUpdateUser_thenReturnStatusCode404() {
		RestAssured.defaultParser = Parser.JSON;
		User user = new User(99l, "Sadio", "Mane", "SM", "password", "s.mane@lfc.com", "Senegal");
		given().contentType(ContentType.JSON).body(user)
				.when().put(BASE_USER_ROUTE + UPDATE_USER_ENDPOINT).then()
				.assertThat().statusCode(equalTo(HttpStatus.NOT_FOUND.value()));
	}
	
	@Test
	@Order(9)
	public void givenGETValidCountry_whenSearchForUsers_thenReturnTwoUsers() {
		final String CRITERIA = "country:England";
		
		given().contentType(ContentType.JSON).when().get(BASE_USER_ROUTE + SEARCH_USER_ENDPOINT + CRITERIA).then()
				.assertThat().body("findAll {it.country == 'England'}", hasSize(2));
	}
	
	@Test
	@Order(10)
	public void givenGETValidCountryAndFirstName_whenSearchForUsers_thenReturnOneUser() {
		final String CRITERIA = "country:England,firstName:Joe";
		
		given().when().get(BASE_USER_ROUTE + SEARCH_USER_ENDPOINT + CRITERIA).then()
				.assertThat().body("$", hasSize(1))
				.assertThat().body("[0].firstName", is("Joe"));
	}
	
	@Test
	@Order(11)
	public void givenGETValidNegationCountry_whenSearchForUsers_thenReturnTwoUsers() {
		final String CRITERIA = "country!England";
		
		given().when().get(BASE_USER_ROUTE + SEARCH_USER_ENDPOINT + CRITERIA).then()
				.assertThat().body("findAll {it.country != 'England'}", hasSize(4));
	}
	
	@Test
	@Order(12)
	public void givenValidUserId_whenRemoveUser_thenReturnStatusCode202() {
		final String USER_ID = "5";
		RestAssured.defaultParser = Parser.JSON;
		given().contentType(ContentType.JSON)
				.when().delete(BASE_USER_ROUTE + DELETE_USER_ENDPOINT + USER_ID).then()
				.assertThat().statusCode(equalTo(HttpStatus.ACCEPTED.value()));
	}
	
	@Test
	@Order(13)
	public void givenInvalidUserId_whenRemoveUser_thenReturnStatusCode404() {
		final String USER_ID = "99";
		RestAssured.defaultParser = Parser.JSON;
		given().contentType(ContentType.JSON)
				.when().delete(BASE_USER_ROUTE + DELETE_USER_ENDPOINT + USER_ID).then()
				.assertThat().statusCode(equalTo(HttpStatus.NOT_FOUND.value()));
	}

}
