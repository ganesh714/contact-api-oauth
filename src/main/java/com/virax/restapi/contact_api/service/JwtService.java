package com.virax.restapi.contact_api.service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.virax.restapi.contact_api.model.Developer;
import com.virax.restapi.contact_api.model.DeveloperUserDetails;
import com.virax.restapi.contact_api.model.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	
	
	@Value("${private.key.path}")
	private String privateKeyFilePathKey;
	
	@Value("${public.key.path}")	
	private String publicKeyFilePathKey;
	
	
	public PrivateKey loadPrivateKey(String privateKeyPath) {
		try {
			Resource resource = new ClassPathResource(privateKeyPath);
			
			String key= new String(resource.getInputStream().readAllBytes());
			
			key =  key.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s+", "");
			
			byte[] keyBytes = Base64.getDecoder().decode(key);
			
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			
			return keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			throw new RuntimeException("unable to load Pri K", e);
		}
	}
	
	public PublicKey loadPublicKey(String publicKey) {
		
		try {
			
			Resource resource = new ClassPathResource(publicKey);
			
			String key = new String(resource.getInputStream().readAllBytes());
			
			key = key.replace("-----BEGIN PUBLIC KEY-----", "")
				.replace("-----END PUBLIC KEY-----", "")
				.replaceAll("\\s+", "");
			
			byte[] keyBytes = Base64.getDecoder().decode(key);
			
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			
			return keyFactory.generatePublic(keySpec);
				
		} catch (Exception e) {
			throw new RuntimeException("cant load Pub K", e);
		}
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
				.signWith(loadPrivateKey(privateKeyFilePathKey))
				.compact();
		
	}

	
	public <T> T extractClaims(String jwtToken, Function<Claims, T> typeOfClaim) {
		
		Claims claims = Jwts.parser()
				.verifyWith(loadPublicKey(publicKeyFilePathKey))
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
