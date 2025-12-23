package com.example.social.security;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.Role;
import com.example.social.domain.User;
import com.example.social.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleAuthorizationAspect {

    private final UserRepository userRepository;

    @Around("@annotation(com.example.social.security.RequiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresRole requiresRole = method.getAnnotation(RequiresRole.class);

        if (requiresRole == null) {
            return joinPoint.proceed();
        }

        // Get current user
        String email = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        // Check if user has required role
        Role[] requiredRoles = requiresRole.value();
        boolean hasRole = Arrays.asList(requiredRoles).contains(user.getRole());

        if (!hasRole) {
            throw new AccessDeniedException("Insufficient permissions. Required roles: " +
                    Arrays.toString(requiredRoles));
        }

        return joinPoint.proceed();
    }
}
