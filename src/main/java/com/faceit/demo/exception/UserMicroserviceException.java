package com.faceit.demo.exception;

public class UserMicroserviceException extends RuntimeException{

	public UserMicroserviceException(String msg) {
        super(msg);
    }

}
