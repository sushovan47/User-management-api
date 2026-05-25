package com.demo.practice.bean;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demo.practice.service.CustomUserDetailsService;
import com.demo.practice.service.JWTService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JWTService jwtService;
	private final CustomUserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JWTService jwtService, CustomUserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");
		final String jwt;
		final String username;

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		jwt = authHeader.substring(7);
		username = jwtService.extractUsername(jwt);

		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			try {
				if (jwtService.isTokenValid(jwt, userDetails)) {

					Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities().stream()
							.map(role -> new SimpleGrantedAuthority(role.getAuthority())).collect(Collectors.toList());

					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, authorities);

					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authToken);
				} else {
					request.setAttribute("expired", "Invalid token");
				}
			} catch (ExpiredJwtException e) {
				request.setAttribute("expired", "Your token is expired");
			} catch (Exception e) {
				request.setAttribute("expired", "Invalid token");
			}
		} else {
			request.setAttribute("expired", "Invalid token");
		}
		filterChain.doFilter(request, response);
	}
}