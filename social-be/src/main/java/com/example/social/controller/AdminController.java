package com.example.social.controller;

import com.example.social.controller.error.ResourceNotFoundException;
import com.example.social.domain.Post;
import com.example.social.domain.Role;
import com.example.social.dto.request.admin.ReqUpdateRoleDTO;
import com.example.social.dto.response.admin.ResAdminStatsDTO;
import com.example.social.dto.response.admin.ResUserAdminDTO;
import com.example.social.security.RequiresRole;
import com.example.social.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @RequiresRole({ Role.ADMIN, Role.MODERATOR })
    public ResponseEntity<Page<ResUserAdminDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ResUserAdminDTO> users = adminService.getAllUsers(pageable, search);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/role")
    @RequiresRole(Role.ADMIN)
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long id,
            @RequestBody ReqUpdateRoleDTO request) throws ResourceNotFoundException {

        adminService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    @RequiresRole(Role.ADMIN)
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) throws ResourceNotFoundException {
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts")
    @RequiresRole({ Role.ADMIN, Role.MODERATOR })
    public ResponseEntity<Page<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = adminService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @DeleteMapping("/posts/{id}")
    @RequiresRole({ Role.ADMIN, Role.MODERATOR })
    public ResponseEntity<Void> deletePost(@PathVariable Long id) throws ResourceNotFoundException {
        adminService.deletePost(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{id}")
    @RequiresRole({ Role.ADMIN, Role.MODERATOR })
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) throws ResourceNotFoundException {
        adminService.deleteComment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    @RequiresRole({ Role.ADMIN, Role.MODERATOR })
    public ResponseEntity<ResAdminStatsDTO> getStatistics() {
        ResAdminStatsDTO stats = adminService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}
