package com.faceit.demo.exception;

public class UserNotFoundException extends UserMicroserviceException {
	
	public UserNotFoundException(long id) {
        super(String.format("User with Id %d not found", id));
    }

}
