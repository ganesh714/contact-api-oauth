package com.virax.restapi.contact_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.virax.restapi.contact_api.model.Developer;
import com.virax.restapi.contact_api.model.DeveloperUserDetails;
import com.virax.restapi.contact_api.repository.DeveloperRespository;


@Service
public class DeveloperUserDetailsService implements UserDetailsService{
	
	@Autowired
	DeveloperRespository developerRespository;
	
	@Override
	public DeveloperUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Developer dev = developerRespository.findByUserName(username);
		if (dev == null) {
			throw new UsernameNotFoundException(username);
		}
		return new DeveloperUserDetails(dev);
	}

}
