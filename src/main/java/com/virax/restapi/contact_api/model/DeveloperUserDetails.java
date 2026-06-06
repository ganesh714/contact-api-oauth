package com.virax.restapi.contact_api.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class DeveloperUserDetails implements UserDetails{

	private Developer developer;
	
	public DeveloperUserDetails(Developer developer) {
		this.developer = developer;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
		Set<Role> roles = developer.getRoles();
		for (Role role : roles)
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
		
		return authorities;
	}

	@Override
	public String getPassword() {
		return developer.getPassword();
	}

	@Override
	public String getUsername() {
		return developer.getUserName();
	}
	
	public Developer getUser() {
		return developer;
	}
}
