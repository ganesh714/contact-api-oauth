package com.virax.restapi.contact_api.service;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.virax.restapi.contact_api.model.Developer;
import com.virax.restapi.contact_api.model.DeveloperUserDetails;
import com.virax.restapi.contact_api.model.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	
	private String secretKey = "";
	
	public JwtService() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
		SecretKey secretKey = keyGenerator.generateKey();
		this.secretKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
	}
	
	public String generateToken(Developer user) {
		
		Map<String, Object> claims = new HashMap<>();
		claims.put("email", user.getUserName()+"@gmail.com");
		claims.put("roles", user.getRoles()
								.stream()
								.map(Role::getName)
								.toList());
		
		return Jwts.builder()
				.claims(claims)
				.subject(user.getUserName())
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
				.signWith(getKey())
				.compact();
		
	}
	
	public SecretKey getKey() {
		byte[] bytesKey = Base64.getDecoder().decode(secretKey);
		return Keys.hmacShaKeyFor(bytesKey);
	}
	
	public <T> T extractClaims(String jwtToken, Function<Claims, T> typeOfClaim) {
		
		Claims claims = Jwts.parser()
				.verifyWith(getKey())
				.build()
				.parseSignedClaims(jwtToken)
				.getPayload();
		return typeOfClaim.apply(claims);
	}
	
	public String extractUserName(String jwtToken) {
		return extractClaims(jwtToken, Claims::getSubject);
	}
	
	public boolean isTokenExpired(String jwtToken) {
		Date expireDate = extractClaims(jwtToken, Claims::getExpiration);
		return expireDate.before(new Date());
	}
	
	public boolean validateToken(String jwtToken, DeveloperUserDetails userDetails) {
		String userName = extractUserName(jwtToken);
		
		if (userName.equals(userDetails.getUsername()) && !isTokenExpired(jwtToken)) {
			return true;
		}
		return false;
	}
}
