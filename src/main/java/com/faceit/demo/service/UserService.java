package com.faceit.demo.service;

import com.faceit.demo.entity.User;
import com.faceit.demo.exception.NoDataFoundException;
import com.faceit.demo.exception.UserNotCreatedException;
import com.faceit.demo.exception.UserNotFoundException;

public interface UserService {
	
	/**
     * To list the complete data set from the User repository.
     *
     * @return  an {@link Iterable} of {@link User} entities
     */
	public Iterable<User> findAll() throws NoDataFoundException;

	/**
	 * To create user 
	 * 
	 * @param user user to be created
	 * @return {@link User} created user
	 * @throws UserNotCreatedException Error creating user
	 */
	public User create(User user) throws UserNotCreatedException;
	
	/**
	 * To find a specific {@link User} entity by its ID.
	 * 
	 * @param id  ID of target user
	 * @return {@link User} user with provided ID
	 * @throws UserNotFoundException User not found
	 */
	public User findById(long id) throws UserNotFoundException;
	
	/**
	 * To update a user
	 * 
	 * @param user user to be updated
	 * @return {@link User} updated user
	 */
	public User update(User user);
	
	/**
	 * To remove a user by ID
	 * 
	 * @param id  ID of target user
	 * @throws UserNotFoundException User not found
	 * @throws NoDataFoundException Error deleting {@link User}
	 */
	public void removeById(long id) throws UserNotFoundException, NoDataFoundException; 
	
	/**
	 * To search a list of {@link User} entities using search criteria
	 * 
	 * @param critera a comma-separated search string with following format: 
	 * {key}{operator}{value}. e.g. firstName:Joe,country!England. Which means: firstName
	 * EQUALS Joe, AND country IS-NOT England. Available {operators} are - ":" = EQUALS;
	 *  "!" = IS-NOT;
	 * 					
	 * @return an {@link Iterable} of {@link User} entities
	 */
	public Iterable<User> search(String critera);

}
