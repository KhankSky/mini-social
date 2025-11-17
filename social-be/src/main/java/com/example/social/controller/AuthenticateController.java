package com.example.social.controller;

import com.example.social.domain.User;
import com.example.social.dto.request.auth.ReqLoginDTO;
import com.example.social.dto.response.user.ResGetUserDTO;
import com.example.social.service.AuthenticateService;
import com.example.social.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.social.security.SecurityUtils;

@RestController
@RequestMapping("/api")
public class AuthenticateController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final AuthenticateService authenticateService;
    private final UserService userService;

    public AuthenticateController(AuthenticationManagerBuilder authenticationManagerBuilder,
                                  AuthenticateService authenticateService,
                                  UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.authenticateService = authenticateService;
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<String> Login(@Valid @RequestBody ReqLoginDTO reqLogin) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                reqLogin.getUsername(),
                reqLogin.getPassword()
        );

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResGetUserDTO userDB = this.userService.getUserByUsername(reqLogin.getUsername());
        String jwt = this.authenticateService.createToken(reqLogin.getUsername(), userDB);
        return ResponseEntity.ok(jwt);
    }

    @GetMapping("/auth/me")
    public ResponseEntity<ResGetUserDTO> me() {
        String username = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        ResGetUserDTO user = this.userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
}
