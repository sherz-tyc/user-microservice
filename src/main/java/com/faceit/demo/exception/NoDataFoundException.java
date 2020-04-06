package com.faceit.demo.exception;

public class NoDataFoundException extends UserMicroserviceException {

	public NoDataFoundException(String message) {
		super(message);
	}
	
	public NoDataFoundException() {
		super("Search returns no result.");
	}


}
