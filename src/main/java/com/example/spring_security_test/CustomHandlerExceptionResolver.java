package com.example.spring_security_test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CustomHandlerExceptionResolver implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        System.out.println("Handler Exception Resolver called...");
        if (ex instanceof AuthenticationException) {
            // Handle 401 Unauthorized errors
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            try {
                response.getWriter().write("{\"error\": \"Unauthorized Access - Invalid or missing authentication token\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ModelAndView();  // Return empty view as the response is already written
        } else if (ex instanceof AccessDeniedException) {
            // Handle 403 Forbidden errors
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            try {
                response.getWriter().write("{\"error\": \"Forbidden Access - You do not have permission to access this resource\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ModelAndView();  // Return empty view as the response is already written
        }

        return null;  // Let other resolvers handle exceptions
    }
}
