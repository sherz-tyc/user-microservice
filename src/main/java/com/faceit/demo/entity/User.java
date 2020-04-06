package com.faceit.demo.entity;

import javax.persistence.*;

@Entity
public class User {
	
	@Id
	@GeneratedValue
	private long id;
	private String firstName;
	private String lastName;
	private String nickName;
	private String password;
	private String email;
	private String country;
	
	public User() {};
	
	public User(long id, String firstName, String lastName, String nickName,
			String password, String email, String country) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.nickName = nickName;
		this.password = password;
		this.email = email;
		this.country = country;
	}
	
	public User(String firstName, String lastName, String nickName,
			String password, String email, String country) {
		super();
		this.firstName = firstName;
		this.lastName = lastName;
		this.nickName = nickName;
		this.password = password;
		this.email = email;
		this.country = country;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
}
