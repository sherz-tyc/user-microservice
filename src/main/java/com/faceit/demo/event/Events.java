package com.faceit.demo.event;

public enum Events {
	
	USER_CREATED("USER CREATED"),
    USER_UPDATED("USER UPDATED"),
	USER_DELETED("USER DELETED");

    public String stringVal;

    Events(String event) {
        this.stringVal = event;
    }

}
