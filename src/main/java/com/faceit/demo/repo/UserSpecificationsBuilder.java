package com.faceit.demo.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;

import com.faceit.demo.entity.User;
import com.faceit.demo.util.SpecSearchCriteria;

/**
 * Joins accumulated specifications centrally.
 */
public class UserSpecificationsBuilder {
	private final List<SpecSearchCriteria> params;
	 
    public UserSpecificationsBuilder() {
        params = new ArrayList<SpecSearchCriteria>();
    }
 
    public UserSpecificationsBuilder with(String key, String operation, Object value) {
        params.add(new SpecSearchCriteria(key, operation, value));
        return this;
    }
 
    public Specification<User> build() {
        if (params.size() == 0) {
            return null;
        }
 
        List<Specification<User>> specs = params.stream()
          .map(UserSpecification::new)
          .collect(Collectors.toList());
         
        Specification<User> result = specs.get(0);
 
        for (int i = 1; i < params.size(); i++) {
            result = Specification.where(result).and(specs.get(i));
        }       
        return result;
    }

}
