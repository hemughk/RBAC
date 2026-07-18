package com.example.rbac.service;

import com.example.rbac.dto.UserRegistrationDTO;
import com.example.rbac.model.Role;
import com.example.rbac.model.User;
import com.example.rbac.repository.RoleRepository;
import com.example.rbac.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Create a new user (called by Super Admin)
     */
    public User createUser(UserRegistrationDTO dto, User createdBy) throws Exception {
        // Check if user already exists
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new Exception("Username already exists");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new Exception("Email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setIsApproved(false);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        // Get role
        Role role = roleRepository.findByName(dto.getRoleName())
                .orElseThrow(() -> new Exception("Role not found"));
        user.setRole(role);

        // Assign to product admin or auto-assign to creator if no product admin exists
        if ("PRODUCT_ADMIN".equals(dto.getRoleName())) {
            user.setProductAdmin(null); // Product admins have no parent
        } else {
            List<User> productAdmins = userRepository.findByRoleNameAndIsApprovedTrue("PRODUCT_ADMIN");
            if (!productAdmins.isEmpty()) {
                user.setProductAdmin(productAdmins.get(0)); // Assign to first available product admin
            } else {
                user.setProductAdmin(createdBy); // Auto-assign to creator if no product admin
            }
        }

        return userRepository.save(user);
    }

    /**
     * Approve a user (Super Admin only)
     */
    public User approveUser(Long userId, User approvedBy) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        user.setIsApproved(true);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Get all pending users for approval
     */
    public List<User> getPendingUsers() {
        return userRepository.findByIsApprovedFalse();
    }

    /**
     * Get all users with a specific role
     */
    public List<User> getUsersByRole(String roleName) {
        return userRepository.findByRoleName(roleName);
    }

    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}