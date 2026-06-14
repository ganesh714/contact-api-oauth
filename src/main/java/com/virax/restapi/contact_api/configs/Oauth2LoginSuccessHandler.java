package com.virax.restapi.contact_api.configs;

import java.io.IOException;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.virax.restapi.contact_api.model.Developer;
import com.virax.restapi.contact_api.repository.DeveloperRespository;
import com.virax.restapi.contact_api.service.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class Oauth2LoginSuccessHandler implements AuthenticationSuccessHandler{

	
	@Autowired
	DeveloperRespository developerRespository;
	
	@Autowired
	@Lazy
	PasswordEncoder passwordEncoder;
	
	@Autowired
	JwtService jwtService;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		OAuth2AuthenticationToken token =  (OAuth2AuthenticationToken) authentication;
		
		OAuth2User user = token.getPrincipal();
		
		String email = user.getAttribute("email");
		String githubLogin = user.getAttribute("login");
		
		String userName = (email != null) ? email.split("@")[0] : githubLogin;
		
		Developer developer = developerRespository.findByUserName(userName);
		
		if (developer == null) {
			developer = new Developer();
			developer.setId(userName.charAt(0) + userName.charAt(1));
			developer.setPassword(passwordEncoder.encode("OAUTH_DUMMY"));
			developer.setRoles(new HashSet<>());
			
			developer = developerRespository.save(developer);
		}
		
		String jwtToken = jwtService.generateToken(developer);
		
		response.setContentType("application/json");
        response.getWriter().write("{\"token\": \"" + jwtToken + "\"}");
	}
	
}
