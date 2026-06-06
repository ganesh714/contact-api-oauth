package com.virax.restapi.contact_api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.virax.restapi.contact_api.dtos.DeveloperDto;
import com.virax.restapi.contact_api.dtos.TokenUserDto;
import com.virax.restapi.contact_api.mappers.DeveloperMapper;
import com.virax.restapi.contact_api.model.Developer;
import com.virax.restapi.contact_api.model.DeveloperUserDetails;
import com.virax.restapi.contact_api.repository.DeveloperRespository;

@Service
public class DeveloperService {
	
	@Autowired
	private DeveloperRespository developerRespository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private DeveloperMapper developerMapper; 
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JwtService jwtService;
	
	public DeveloperDto addDeveloper(DeveloperDto developerDto) {
		if (developerRespository.findById(developerDto.getId()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Developer already exists");
		}
		
		Developer developer = developerMapper.toDeveloper(developerDto);
		developer.setPassword(passwordEncoder.encode(developer.getPassword()));
		
		Developer savedDev = developerRespository.save(developer);
		return developerMapper.toDeveloperDto(savedDev); 
	}

	public DeveloperDto updateDeveloper(int id, DeveloperDto developerDto) {
		Developer existingDev = developerRespository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer not found"));

		existingDev.setUserName(developerDto.getUserName());
		
		if (developerDto.getPassword() != null && !developerDto.getPassword().isEmpty()) {
			existingDev.setPassword(passwordEncoder.encode(developerDto.getPassword()));
		}
		
		Developer mappedDev = developerMapper.toDeveloper(developerDto);
		existingDev.setRoles(mappedDev.getRoles());

		Developer updatedDev = developerRespository.save(existingDev);
		return developerMapper.toDeveloperDto(updatedDev);
	}

	public List<DeveloperDto> getAllDevelopers() {
        return developerRespository.findAll()
        		.stream()
        		.map(developerMapper::toDeveloperDto)
        		.collect(Collectors.toList());
    }

    public DeveloperDto getDeveloperByUserName(String userName) {
        Developer dev = developerRespository.findByUserName(userName);
        if (dev == null) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer not found");
        }
        return developerMapper.toDeveloperDto(dev);
    }
    
    public void deleteDeveloper(int id) {
    	if (!developerRespository.existsById(id)) {
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer not found");
    	}
        developerRespository.deleteById(id);
    }

	public String generateToken(TokenUserDto tokenUserDto) {
		
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(
					tokenUserDto.getUserName(),
					tokenUserDto.getPassword()
			)
		);
		
		if (authentication.isAuthenticated()) {
			DeveloperUserDetails userDetails = (DeveloperUserDetails) authentication.getPrincipal();
			Developer user = userDetails.getUser();
			return jwtService.generateToken(user);
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid details");
	}
}