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
import com.example.social.domain.User;
import com.example.social.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.social.repository.FriendRequestRepository;
import com.example.social.repository.FriendsRepository;
import com.example.social.domain.FriendRequest;
import com.example.social.domain.FriendRequestStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendsRepository friendsRepository;

    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            FriendRequestRepository friendRequestRepository,
            FriendsRepository friendsRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.friendRequestRepository = friendRequestRepository;
        this.friendsRepository = friendsRepository;
    }

    public ResCreateUserDTO createUser(ReqCreateUserDTO reqUser) throws ResourceAlreadyExistsException {
        log.info("Request to create user with email: {}", reqUser.getEmail());
        // Check duplicate email
        if (this.userRepository.existsByEmail(reqUser.getEmail())) {
            log.warn("User creation failed: Email {} already exists", reqUser.getEmail());
            throw new ResourceAlreadyExistsException("User with email " + reqUser.getEmail() + " already exists");
        }

        // Check duplicate username
        if (reqUser.getUsername() != null && !reqUser.getUsername().isEmpty()
                && this.userRepository.existsByUsername(reqUser.getUsername())) {
            log.warn("User creation failed: Username {} already exists", reqUser.getUsername());
            throw new ResourceAlreadyExistsException("User with username " + reqUser.getUsername() + " already exists");
        }

        User user = User.builder()
                .username(reqUser.getUsername())
                .password(this.passwordEncoder.encode(reqUser.getPassword()))
                .email(reqUser.getEmail())
                .avatarUrl(reqUser.getAvatarUrl())
                .bio(reqUser.getBio())
                .build();

        User savedUser = this.userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

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
        log.info("Request to update user with ID: {}", reqUser.getId());
        User userDB = userRepository.findById(reqUser.getId())
                .orElseThrow(() -> {
                    log.error("User update failed: User not found with id = {}", reqUser.getId());
                    return new ResourceNotFoundException("User not found with id = " + reqUser.getId());
                });

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
        log.info("User updated successfully: {}", userSaved.getUsername());

        return ResUpdateUserDTO.builder()
                .id(userSaved.getId())
                .username(userSaved.getUsername())
                .avatarUrl(userSaved.getAvatarUrl())
                .bio(userSaved.getBio())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        log.debug("Fetching all users with pagination: page={}, size={}", pageable.getPageNumber(),
                pageable.getPageSize());
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
        ResGetUserDTO dto = ResGetUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        // Check relationship status if user is authenticated
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (currentUserEmail != null && !currentUserEmail.equals("anonymousUser")) {
            User currentUser = userRepository.findByEmail(currentUserEmail);
            if (currentUser != null && !currentUser.getId().equals(user.getId())) {
                // Check if already friends
                if (friendsRepository.areFriends(currentUser, user)) {
                    dto.setRelationStatus("FRIEND");
                } else {
                    // Check for pending requests
                    Optional<FriendRequest> request = friendRequestRepository.findRequestBetweenUsers(currentUser,
                            user);
                    if (request.isPresent()) {
                        FriendRequest req = request.get();
                        if (req.getStatus() == FriendRequestStatus.PENDING) {
                            if (req.getSender().getId().equals(currentUser.getId())) {
                                dto.setRelationStatus("PENDING_SENT");
                            } else {
                                dto.setRelationStatus("PENDING_RECEIVED");
                            }
                        } else {
                            dto.setRelationStatus("NONE");
                        }
                    } else {
                        dto.setRelationStatus("NONE");
                    }
                }
            } else if (currentUser != null && currentUser.getId().equals(user.getId())) {
                dto.setRelationStatus("SELF");
            }
        }

        return dto;
    }

    public ResGetUserDTO getUserById(Long id) throws ResourceNotFoundException {
        log.debug("Fetching user by ID: {}", id);
        User userDB = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id = " + id));
        return toGetUserDTO(userDB);
    }

    public void deleteUserById(Long id) throws ResourceNotFoundException {
        log.info("Request to delete user with ID: {}", id);
        User userDB = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Delete failed: User not found with id = {}", id);
                    return new ResourceNotFoundException("User not found with id = " + id);
                });
        userRepository.delete(userDB);
        log.info("User deleted successfully with ID: {}", id);
    }

    public ResGetUserDTO getUserByUsername(String email) {
        log.debug("Fetching user by email: {}", email);
        return this.toGetUserDTO(this.userRepository.findByEmail(email));
    }

    public void updateLastLogin(String email) {
        User user = this.userRepository.findByEmail(email);
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            this.userRepository.save(user);
            log.debug("Updated last login for user: {}", email);
        }
    }

    public void changePassword(String email, com.example.social.dto.request.user.ReqChangePasswordDTO req)
            throws ResourceNotFoundException {
        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }
}
