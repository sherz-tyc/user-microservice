package com.faceit.demo.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.faceit.demo.entity.User;
import com.faceit.demo.event.EventPublisher;
import com.faceit.demo.event.Events;
import com.faceit.demo.exception.NoDataFoundException;
import com.faceit.demo.exception.UserNotCreatedException;
import com.faceit.demo.exception.UserNotFoundException;
import com.faceit.demo.repo.UserRepository;
import com.faceit.demo.repo.UserSpecificationsBuilder;

@Service
public class UserServiceImpl implements UserService {
	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private EventPublisher eventPublisher;
	
	private static final String ERROR_USER_ID = "User must not be supplied with ID";
	private static final String ERROR_USER_OBJ = "Error processing User entity";
	private static final String PATTERN_MATCHING_REGEX = "(\\w+?)(:|!)(\\w+?),";
	
	@Override
	public Iterable<User> findAll() throws NoDataFoundException {
		
		Iterable<User> ships = userRepo.findAll();
        if (ships.spliterator().getExactSizeIfKnown() <= 0) {
            throw new NoDataFoundException();
        }
        return ships;
	}

	@Override
	public User create(User user) throws UserNotCreatedException {
		
		if (user.getId() > 0) {
			throw new UserNotCreatedException(String.format(ERROR_USER_ID, user.getId()));
		} 
		
		User result;
		try {
			result = userRepo.save(user);
		} catch (IllegalArgumentException ex) {
			throw new UserNotCreatedException(ERROR_USER_OBJ);
		}
		
		// Produce message to be published via Kafka
		try {
			eventPublisher.publishEvent(Events.USER_CREATED, result);
		} catch (RuntimeException ex) {
			/* TODO: Handle exceptions gracefully without affecting main function
			*  of creating User in repository.
			*/
			LOG.error("Error producing USER_CREATED message via Event Publisher.");
		}
		return result;
		
	}

	@Override
	public User findById(long id) throws UserNotFoundException {
		
		return userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	@Override
	public User update(User user) throws UserNotFoundException {
		User result;
		if (userRepo.existsById(user.getId())) {
			result = userRepo.save(user);
		} else {
			throw new UserNotFoundException(user.getId());
		}
		
		// Produce message to be published via Kafka
		try {
			eventPublisher.publishEvent(Events.USER_UPDATED, result);
		} catch (RuntimeException ex) {
			/* TODO: Handle exceptions gracefully without affecting main function
			*  of updating User in repository.
			*/
			LOG.error("Error producing USER_UPDATED message via Event Publisher.");
		}
		return result;
	}

	@Override
	public void removeById(long id) {
		try {
			userRepo.deleteById(id);
        } catch (IllegalArgumentException ex) {
        	throw new NoDataFoundException("Must provide a valid User ID.");
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException(id);
        }
		
		// Produce message to be published via Kafka
		try {
			eventPublisher.publishEvent(Events.USER_DELETED, String.valueOf(id));
		} catch (RuntimeException ex) {
			/* TODO: Handle exceptions gracefully without affecting main function
			*  of deleting User from repository.
			*/
			LOG.error("Error producing USER_DELETED message via Event Publisher.");
		}
		
	}

	@Override
	public Iterable<User> search(String critera) {
		UserSpecificationsBuilder builder = new UserSpecificationsBuilder();
		
		/* To de-construct comma-separated search criteria by matching patterns
		 * Added Pattern.UNICODE_CHARACTER_CLASS to support non-English systems */
        Pattern pattern = Pattern.compile(PATTERN_MATCHING_REGEX, Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(critera + ",");
        
        while (matcher.find()) {
        	// Accumulate search criteria: 1=key; 2=operator; 3=value;
            builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
        }
        
        Specification<User> spec = builder.build();
        return userRepo.findAll(spec);
	}

}
