package com.example.social.repository;

import com.example.social.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);

    User findByEmail(String email);

    List<User> email(String email);

    // Admin queries
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Pageable pageable);

    long countByLastLoginAfter(LocalDateTime date);

    long countByCreatedAtAfter(LocalDateTime date);
}
