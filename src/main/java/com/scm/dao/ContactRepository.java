package com.scm.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.scm.entities.Contact;
import com.scm.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	// used for pagination
	
	
	//custom method to get selected user contact list only
	@Query("from Contact as c where c.user.id=:userId")
	//Page return sublist of List of objects
	public Page<Contact> findContactsByUser(@Param("userId")int userId, Pageable pageable);
	// Pageable has two information - 
	// current page no = [page] : 0 
	// contact per page = [n] : 5
	
	// search method
	 public List<Contact> findByNameContainingAndUser(String name, User user);
}
