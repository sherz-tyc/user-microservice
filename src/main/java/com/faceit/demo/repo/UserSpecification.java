package com.faceit.demo.repo;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.faceit.demo.entity.User;
import com.faceit.demo.util.SpecSearchCriteria;

/**
 * Define specifications, which includes a (key - operator - value} set
 */
public class UserSpecification implements Specification<User> {

	private static final long serialVersionUID = -1111332731614400363L;
	
	private static final String EQUALITY = ":";
	private static final String NEGATION = "!";
	 
	private SpecSearchCriteria criteria;
	
	public UserSpecification(SpecSearchCriteria criteria) {
		super();
		this.criteria = criteria;
	}

	/**
	 * TODO: can expand to handle much more operations such as CONTAIN, STARTS_WITH, etc.
	 */
	@Override
	public Predicate toPredicate(final Root<User> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
		switch (criteria.getOperation()) {
		case EQUALITY:
			return builder.equal(root.get(criteria.getKey()), criteria.getValue());
		case NEGATION:
			return builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
//		case CONTAINS:
//			return builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
		default:
			return null;
		}
	}

}
