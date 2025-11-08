package com.example.social.controller;

import com.example.social.controller.error.ResourceAlreadyExistsException;
import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.dto.request.user.ReqCreateUserDTO;
import com.example.social.dto.request.user.ReqUpdateUserDTO;
import com.example.social.dto.response.filter.ResultPaginationDTO;
import com.example.social.dto.response.user.ResCreateUserDTO;
import com.example.social.dto.response.user.ResGetUserDTO;
import com.example.social.dto.response.user.ResUpdateUserDTO;
import com.example.social.entity.User;
import com.example.social.repository.UserRepository;
import com.example.social.service.UserService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/users")
    public ResponseEntity<ResCreateUserDTO> createUser(@Valid @RequestBody ReqCreateUserDTO reqUser) throws ResourceAlreadyExistsException {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.createUser(reqUser));
    }

    @PutMapping("/users")
    public ResponseEntity<ResUpdateUserDTO> updateUser(@Valid @RequestBody ReqUpdateUserDTO reqUser) throws ResourceNotFoundException {
        return ResponseEntity.ok(this.userService.updateUser(reqUser));
    }

    @GetMapping("/users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(@Filter Specification<User> specUser, Pageable pageable) {
        return ResponseEntity.ok(this.userService.getAllUsers(specUser, pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ResGetUserDTO> getUserById(@PathVariable Long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(this.userService.getUserById(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) throws ResourceNotFoundException {
        this.userService.deleteUserById(id);
        return ResponseEntity.ok(null);
    }
}
