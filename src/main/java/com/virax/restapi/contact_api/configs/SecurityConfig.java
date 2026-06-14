package com.virax.restapi.contact_api.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.virax.restapi.contact_api.service.DeveloperUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity 
public class SecurityConfig {

	@Autowired
	DeveloperUserDetailsService developerUserDetailsService;
	
	@Autowired
	JwtFilter jwtFilter;
	
	@Autowired
	Oauth2LoginSuccessHandler oauth2LoginSuccessHandler;
	
	@Bean
	public SecurityFilterChain getSecurityFilterChain(HttpSecurity http) throws Exception {

		return http.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						.dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll() 
						.requestMatchers(
								"/api/developers/add",
								"/generateToken",
								"/user/login",
								"/login/**",
								"/oauth2/**"
								).permitAll()
						.anyRequest().authenticated()) 
				.httpBasic(Customizer.withDefaults())
				.oauth2Login(oauth2 -> oauth2.successHandler(oauth2LoginSuccessHandler))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider auth = new DaoAuthenticationProvider(developerUserDetailsService);
		auth.setPasswordEncoder(passwordEncoder);

		return auth;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public RoleHierarchy roleHierarchy() {
		RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
		
		hierarchy.setHierarchy("""
				ROLE_ADMIN > ROLE_MANAGER
				ROLE_MANAGER > ROLE_USER
			""");
		
		return hierarchy;
	}
}
