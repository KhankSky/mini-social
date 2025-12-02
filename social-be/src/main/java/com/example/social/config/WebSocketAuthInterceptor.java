package com.example.social.config;

import com.example.social.security.SecurityUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtDecoder jwtDecoder;

    public WebSocketAuthInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            // Lấy token từ query parameter hoặc header
            String token = extractToken(request);
            
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (token != null && !token.isEmpty()) {
                // Decode JWT
                Jwt jwt = jwtDecoder.decode(token);
                String email = jwt.getSubject();
                
                // Tạo Authentication object
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    email, null, null
                );
                
                // Set vào attributes để Spring sử dụng
                attributes.put("SPRING_SECURITY_CONTEXT", SecurityContextHolder.createEmptyContext());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                System.out.println("WebSocket authenticated user: " + email);
                return true;
            } else {
                System.out.println("WebSocket connection without token - allowing anonymous");
                return true; // Cho phép anonymous connection
            }
        } catch (Exception e) {
            System.out.println("WebSocket authentication failed: " + e.getMessage());
            return true; // Vẫn cho phép connect nhưng không có authentication
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Cleanup nếu cần
    }

    private String extractToken(ServerHttpRequest request) {
        // Thử lấy từ query parameter
        URI uri = request.getURI();
        String query = uri.getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }
        
        // Thử lấy từ header Authorization
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null) {
            return authHeader;
        }
        
        return null;
    }
}