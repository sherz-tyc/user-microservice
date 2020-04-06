package com.faceit.demo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.faceit.demo.entity.User;
import com.faceit.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTests {
	
	private static final String BASE_USER_ROUTE = "/user";
	
	private static final String LIST_USERS_ENDPOINT = "/listAll";
	private static final String CREATE_USER_ENDPOINT = "/create";
	private static final String READ_USER_ENDPOINT = "/find/id/";
	
	@Autowired
    private MockMvc mockMvc;
	
	@MockBean
	private UserService mockedUserService;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Test
    void getAllUserShouldReturnListOfUser() throws Exception {
        List<User> userList = new ArrayList<User>();
        userList.add(new User(1L,"Frank", "Lampard", "Super Frank", "password", "f.lampard@cfc.com", "England"));
        userList.add(new User(2L,"Cesar", "Azpilicueta", "Dave", "password", "c.azpi@cfc.com", "Spain"));
        when(mockedUserService.findAll()).thenReturn(userList);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_USER_ROUTE + LIST_USERS_ENDPOINT)
        		.contentType(MediaType.APPLICATION_JSON)
        	).andExpect(jsonPath("$", hasSize(2)));
    }
	
	@Test
    void postValidUserObjShouldReturnNewUserWithId() throws Exception {
        User userFrank = new User("Frank", "Lampard", "Super Frank", "password", "f.lampard@cfc.com", "England");
        User createdUserFrank = new User(1l, "Frank", "Lampard", "Super Frank", "password", "f.lampard@cfc.com", "England");
        String userFrankJSON = objectMapper.writeValueAsString(userFrank);
        when(mockedUserService.create(Mockito.any(User.class))).thenReturn(createdUserFrank);
        
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(BASE_USER_ROUTE + CREATE_USER_ENDPOINT)
        		.contentType(MediaType.APPLICATION_JSON)
        		.content(userFrankJSON));
        		
        result.andExpect(jsonPath("id").value(1))
        	.andExpect(jsonPath("firstName").value("Frank"))
        	.andExpect(jsonPath("lastName").value("Lampard"))
        	.andExpect(jsonPath("nickName").value("Super Frank"))
        	.andExpect(jsonPath("password").value("password"))
        	.andExpect(jsonPath("email").value("f.lampard@cfc.com"))
        	.andExpect(jsonPath("country").value("England"));
    }
	
	@Test
	void getValidUserObjByIdShouldReturUserWithId() throws Exception {
		User userFrank = new User(1l, "Frank", "Lampard", "Super Frank", "password", "f.lampard@cfc.com", "England");
		String userFrankJSON = objectMapper.writeValueAsString(userFrank);
		when(mockedUserService.findById(Mockito.anyLong())).thenReturn(userFrank);
		
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(BASE_USER_ROUTE + READ_USER_ENDPOINT + 1)
				.contentType(MediaType.APPLICATION_JSON)
				.content(userFrankJSON));
		
		result.andExpect(jsonPath("id").value(1))
		.andExpect(jsonPath("firstName").value("Frank"))
		.andExpect(jsonPath("lastName").value("Lampard"))
		.andExpect(jsonPath("nickName").value("Super Frank"))
		.andExpect(jsonPath("password").value("password"))
		.andExpect(jsonPath("email").value("f.lampard@cfc.com"))
		.andExpect(jsonPath("country").value("England"));
	}
	
	/*
	 * TODO: More unit tests to follow.
	 * 
	 * Comprehensive tests are in UserMicroserviceIT, which has Integration Tests that
	 * covers the UserController.
	 */
	

}
