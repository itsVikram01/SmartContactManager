package com.scm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.scm.dao.UserRepository;
import com.scm.entities.User;

@Service 
public class UserDetailsServiceImpl implements UserDetailsService {

	//Autowired UserRepository dao to use getUserByUserName method by implementing JpaRepository
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	
		// fetching user from database
		User user = userRepository.getUserByUserName(username);
		
		if (user==null) {
			throw new UsernameNotFoundException("Could not found user!!");
		}
		
		UserDetailsImpl userDetailsImpl=new UserDetailsImpl(user);
		
		return userDetailsImpl;
	}
}
