package com.demo.practice.bean;

import java.io.IOException;
import java.util.Enumeration;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.getWriter().write(
				"{ \"error\": \"Unauthorized\", \"message\": \"User doesn't have permission to fetch this data\" }");
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		// Log method and URI
	    System.out.println("Incoming request: " + request.getMethod() + " " + request.getRequestURI());

	    // Log all headers
	    Enumeration<String> headerNames = request.getHeaderNames();
	    while (headerNames.hasMoreElements()) {
	        String headerName = headerNames.nextElement();
	        String headerValue = request.getHeader(headerName);
	        System.out.println(headerName + ": " + headerValue);
	    }
		
		
		String expiredMsg = (String) request.getAttribute("expired");
		expiredMsg = authException.getMessage().equalsIgnoreCase("Bad credentials") ? "Incorrect password !" : authException.getMessage();

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		String message = (expiredMsg != null) ? expiredMsg
				: "Authentication required. Please provide a valid Bearer token.";

		response.getWriter()
				.write(String.format("{ \"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\" }", message));

	}
}
