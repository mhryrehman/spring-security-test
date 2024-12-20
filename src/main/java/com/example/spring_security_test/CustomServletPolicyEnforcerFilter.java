package com.example.spring_security_test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.keycloak.adapters.authorization.integration.jakarta.ServletPolicyEnforcerFilter;
import org.keycloak.adapters.authorization.spi.ConfigurationResolver;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class CustomServletPolicyEnforcerFilter extends ServletPolicyEnforcerFilter {

	public CustomServletPolicyEnforcerFilter(ConfigurationResolver configurationResolver) {
		super(configurationResolver);
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		try {
			super.doFilter(request, response, chain);
		} catch (Exception e) {
			e.printStackTrace();
			if(e.getCause() instanceof HttpResponseException httpResponseException) {
				response.setStatus(httpResponseException.getStatusCode());
				response.setContentType("application/json");
				response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Invalid client credentials\"}");
			} else {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				response.setContentType("application/json");
				response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Authentication failed: Please check logs.\"}");
			}
		}
	}
}
