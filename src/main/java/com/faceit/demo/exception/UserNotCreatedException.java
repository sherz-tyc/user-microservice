package com.faceit.demo.exception;

public class UserNotCreatedException extends UserMicroserviceException {
	
	public UserNotCreatedException(String msg) {
        super(msg);
    }

}
