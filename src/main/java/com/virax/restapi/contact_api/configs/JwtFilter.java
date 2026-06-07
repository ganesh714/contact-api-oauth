package com.virax.restapi.contact_api.configs;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.virax.restapi.contact_api.model.DeveloperUserDetails;
import com.virax.restapi.contact_api.service.DeveloperUserDetailsService;
import com.virax.restapi.contact_api.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter{

	@Autowired
	JwtService jwtService;
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		
		String authHeader = request.getHeader("Authorization");
		String jwtToken = "";
		String userName = "";
		
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			jwtToken = authHeader.substring(7);
			userName = jwtService.extractUserName(jwtToken);
			
			if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				
				DeveloperUserDetails userDetails = applicationContext.getBean(DeveloperUserDetailsService.class).loadUserByUsername(userName);
				if (jwtService.validateToken(jwtToken, userDetails)) {
					UsernamePasswordAuthenticationToken UPAToken = new UsernamePasswordAuthenticationToken(userName,null, userDetails.getAuthorities());
					UPAToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(UPAToken);
				}
			}
		}
		filterChain.doFilter(request, response);
	}
}
