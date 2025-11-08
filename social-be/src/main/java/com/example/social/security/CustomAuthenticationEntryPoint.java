package com.example.social.security;

import com.example.social.dto.response.error.ResAuthError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        ResAuthError authError = new ResAuthError();

        if (authException instanceof BadCredentialsException) {

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            authError.setStatus(HttpStatus.UNAUTHORIZED.value());
            authError.setError("Unauthorized");
            authError.setMessage("Invalid username or password");
        }
        else if (authException instanceof InsufficientAuthenticationException) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            authError.setStatus(HttpStatus.FORBIDDEN.value());
            authError.setError("Forbidden");
            authError.setMessage("Access token is missing, expired, or invalid");
        }
        else {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            authError.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            authError.setError("Internal Server Error");
            authError.setMessage("An unexpected error occurred");
        }

        new ObjectMapper().writeValue(response.getOutputStream(), authError);
    }
}
