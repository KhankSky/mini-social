package com.example.social.service;

import com.example.social.controller.error.ResourceAlreadyExistsException;
import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.dto.request.user.ReqCreateUserDTO;
import com.example.social.dto.request.user.ReqUpdateUserDTO;
import com.example.social.dto.response.filter.Pagination;
import com.example.social.dto.response.filter.ResultPaginationDTO;
import com.example.social.dto.response.user.ResCreateUserDTO;
import com.example.social.dto.response.user.ResGetUserDTO;
import com.example.social.dto.response.user.ResUpdateUserDTO;
import com.example.social.entity.User;
import com.example.social.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResCreateUserDTO createUser(ReqCreateUserDTO reqUser) throws ResourceAlreadyExistsException {
        // Check duplicate email
        if(this.userRepository.existsByEmail(reqUser.getEmail())){
            throw new ResourceAlreadyExistsException("User with email " + reqUser.getEmail() + " already exists");
        }

        User user = User.builder()
                .username(reqUser.getUsername())
                .password(reqUser.getPassword())
                .email(reqUser.getEmail())
                .avatarUrl(reqUser.getAvatarUrl())
                .bio(reqUser.getBio())
                .build();

        User savedUser = this.userRepository.save(user);

        return ResCreateUserDTO.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .avatarUrl(savedUser.getAvatarUrl())
                .bio(savedUser.getBio())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    public ResUpdateUserDTO updateUser(ReqUpdateUserDTO reqUser) throws ResourceNotFoundException {
        User userDB = userRepository.findById(reqUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id = " + reqUser.getId()));

        if (reqUser.getUsername() != null) {
            userDB.setUsername(reqUser.getUsername());
        }

        if (reqUser.getAvatarUrl() != null) {
            userDB.setAvatarUrl(reqUser.getAvatarUrl());
        }

        if (reqUser.getBio() != null) {
            userDB.setBio(reqUser.getBio());
        }

        User userSaved = userRepository.save(userDB);

        return ResUpdateUserDTO.builder()
                .id(userSaved.getId())
                .username(userSaved.getUsername())
                .avatarUrl(userSaved.getAvatarUrl())
                .bio(userSaved.getBio())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> users = this.userRepository.findAll(spec, pageable);

        Pagination pagination = new Pagination();
        pagination.setPage(pageable.getPageNumber() + 1);
        pagination.setSize(pageable.getPageSize());
        pagination.setTotalPages(users.getTotalPages());
        pagination.setTotalElements(users.getTotalElements());

        List<ResGetUserDTO> usersDTO = users.getContent().stream()
                .map(this::toGetUserDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        resultPaginationDTO.setPagination(pagination);
        resultPaginationDTO.setResult(usersDTO);

        return resultPaginationDTO;
    }


    public ResGetUserDTO toGetUserDTO(User user) {
        return ResGetUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public ResGetUserDTO getUserById(Long id) throws ResourceNotFoundException {

        User userDB = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id = " + id));
        return toGetUserDTO(userDB);
    }

    public void deleteUserById(Long id) throws ResourceNotFoundException {

        User userDB = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id = " + id));
        userRepository.delete(userDB);
    }
}
