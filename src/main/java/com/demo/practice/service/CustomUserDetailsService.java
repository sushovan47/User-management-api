package com.demo.practice.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.demo.practice.exception.PracticeAppException;
import com.demo.practice.repository.UserRepo;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepo userRepository;

	public CustomUserDetailsService(UserRepo userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		com.demo.practice.entity.User user = userRepository.findByUserId(username)
				.orElseThrow(() -> new PracticeAppException(
						"User not found: " + "<b>" + username + "</b>" + " please sign up before login"));

		return new User(user.getUserId(), user.getUserCredentials().get(0).getHashPwdCode(),
				List.of(new SimpleGrantedAuthority(user.getUserCredentials().get(0).getRole())));
	}
}