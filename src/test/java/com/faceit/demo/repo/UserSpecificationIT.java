package com.faceit.demo.repo;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIn.in;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.faceit.demo.entity.User;
import com.faceit.demo.util.SpecSearchCriteria;

/**
 * This test specifically tests the search criterion string:
 * A comma-separated search string with following format: 
 * {key}{operator}{value}. e.g. firstName:Joe,country!England. 
 * Which means: firstName EQUALS Joe, AND country IS-NOT England.
 * Available {operators} are - 
 * ":" = EQUALS;
 * "!" = IS-NOT;
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@Rollback
@SpringBootTest
public class UserSpecificationIT {
	
	private static final String EQUALITY = ":";
	private static final String NEGATION = "!";
	
	@Autowired
    private UserRepository userRepo;
	
	private User userFrank;

    private User userCesar;

    private User userJohn;

    @Before
    public void init() {
    	userFrank = new User("Frank", "Lampard", "Super Frank", "password", "f.lampard@cfc.com", "England");
    	userCesar = new User("Cesar", "Azpilicueta", "Dave", "password", "c.azpi@cfc.com", "Spain");
    	userJohn = new User("John", "Terry", "JT", "password", "j.terry@cfc.com", "England");

    	userRepo.save(userFrank);
    	userRepo.save(userCesar);
    	userRepo.save(userJohn);
    }
    
    @After
    public void tearDown() {
    	userRepo.deleteAll();
    }
    
    @Test
    public void givenFirstName_whenGettingListOfUsers_thenReturnTwoUsers() {
        final UserSpecification spec = new UserSpecification(new SpecSearchCriteria("country", EQUALITY, "England"));
        final List<User> results = userRepo.findAll(spec);

        assertThat(userFrank, is(in(results)));
        assertThat(userJohn, is(in(results)));
        assertThat(userCesar, not(in(results)));
    }
    
    @Test
    public void givenCountryAndFirstName_whenGettingListOfUsers_thenReturnOneUser() {
        final UserSpecification spec1 = new UserSpecification(new SpecSearchCriteria("country", EQUALITY, "England"));
        final UserSpecification spec2 = new UserSpecification(new SpecSearchCriteria("firstName", EQUALITY, "Frank"));
        final List<User> results = userRepo.findAll(Specification.where(spec1).and(spec2));

        assertThat(userFrank, is(in(results)));
        assertThat(userJohn, not(in(results)));
        assertThat(userCesar, not(in(results)));
    }
    
    @Test
    public void givenWrongFirstAndLastName_whenGettingListOfUsers_thenReturnNoUser() {
        final UserSpecification spec1 = new UserSpecification(new SpecSearchCriteria("firstName", EQUALITY, "Cesc"));
        final UserSpecification spec2 = new UserSpecification(new SpecSearchCriteria("lastName", EQUALITY, "Fabregas"));
        final List<User> results = userRepo.findAll(Specification.where(spec1).and(spec2));
        
        assertThat(userFrank, not(in(results)));
        assertThat(userJohn, not(in(results)));  
        assertThat(userCesar, not(in(results)));
        assertThat(results.size(), is(0));
    }
    
    @Test
    public void givenExcludeFirstAndLastName_whenGettingListOfUsers_thenReturnOneUser() {
        final UserSpecification spec1 = new UserSpecification(new SpecSearchCriteria("firstName", NEGATION, "John"));
        final UserSpecification spec2 = new UserSpecification(new SpecSearchCriteria("lastName", NEGATION, "Lampard"));
        final List<User> results = userRepo.findAll(Specification.where(spec1).and(spec2));
     
        assertThat(userFrank, not(in(results)));
        assertThat(userJohn, not(in(results)));  
        assertThat(userCesar, is(in(results)));
        assertThat(results.size(), is(1));
    }

}
