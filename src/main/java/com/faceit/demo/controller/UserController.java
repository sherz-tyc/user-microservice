package com.faceit.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.faceit.demo.entity.User;
import com.faceit.demo.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User", description = "The User Microservice API")
@RestController
@RequestMapping("/user")
public class UserController {
	
	private static final String KEY_ID = "id";
    private static final String DELETE_SUCCESS_MSG = "Deleted Successfully.";
	
	@Autowired
	UserService userService;
	
	/**
     * To create a {@link User}.
     *
     * @param  user   {@link User} entity
     */
	@Operation(summary = "Create a user")
	@ApiResponses(value = { 
	        @ApiResponse(responseCode = "200", description = "User created"),
	        @ApiResponse(responseCode = "406", description = "User not created") })
    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody User user) {
    	User createdUser = userService.create(user);
    	
        return new ResponseEntity<User>(createdUser, HttpStatus.CREATED);
    };
    
    /**
     * To return all {@link User} entities.
     * 
     * TODO: To make this end-point scalable, one should use paging techniques
     * to ensure we don't return too many records as User database grows.
     */
    @Operation(summary = "List all users")
    @GetMapping("/listAll")
    public ResponseEntity<Iterable<User>> findAll() {
    	Iterable<User> userList = userService.findAll();
    	
    	return new ResponseEntity<Iterable<User>>(userList, HttpStatus.OK);
    };
    
    /**
     * To find a specific {@link User} entity by its ID.
     *
     * @param  id   unique identifier for the {@link User} entity
     * @return      a {@link User} entity if found
     */
    @Operation(summary = "Finds a user by userId")
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found") })
    @GetMapping("/find/id/{id}")
    public ResponseEntity<User> findById(@PathVariable(KEY_ID) long id) {
        return new ResponseEntity<User>(userService.findById(id), HttpStatus.OK);
    }
    
    /**
     * To remove a specific {@link User} entity by its ID.
     *
     * @param  id   unique identifier for the {@link User} entity
     */
    @Operation(summary = "Deletes a user")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "202", description = "Operation accepted and executed"),
        @ApiResponse(responseCode = "404", description = "Contact not found") })
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<String> deleteById(
    		@Parameter(description="Id of the contact to be delete. Cannot be empty.", required=true)
    		@PathVariable(KEY_ID) long id) {
    	userService.removeById(id);
        return new ResponseEntity<String>(DELETE_SUCCESS_MSG, HttpStatus.ACCEPTED);
    }
    
    /**
     * To update a {@link User}.
     *
     * @param  user   {@link User} entity
     */
    @Operation(summary = "Updates a user")
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "404", description = "User not found") })
    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        return new ResponseEntity<User>(userService.update(user), HttpStatus.OK);
    }
	
    /**
     * To search a list of {@link User} by criteria.
     *
     * @param critera a comma-separated search string with following format: 
	 * {key}{operator}{value}. e.g. firstName:Joe,country!England. Which means: firstName 
	 * EQUALS Joe, AND country IS-NOT England. Available {operators} are - ":" = EQUALS;
	 *  "!" = IS-NOT;
	 * 					
	 * @return an {@link Iterable} of {@link User} entities
     */
    @Operation(summary = "To search a list of User entities using search criteria")
    @GetMapping("/search")
    public ResponseEntity<Iterable<User>> searchUsers(
    		@Parameter(description="a comma-separated search string with following format:"
    				+ " {key}{operator}{value}. e.g. firstName:Joe,country!England. "
    				+ " Which means: firstName EQUALS Joe, AND country IS-NOT England. "
    				+ "Available {operators} are - \":\" = EQUALS;\"!\" = IS-NOT;", required=false)
    		@RequestParam(value = "criteria") String criteria) {
    	Iterable<User> userList = userService.search(criteria);
        return new ResponseEntity<Iterable<User>>(userList, HttpStatus.OK);
    }
	

}
