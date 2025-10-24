package com.group7.DMS.service;

import com.group7.DMS.entity.Users;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    Users findByUsername(String username);
    Users findByEmail(String email);
    Users findByUsernameOrEmail(String username, String email);
    Users save(Users user);
    Users update(Users user);
    void delete(int id);
    List<Users> findAll();
    List<Users> findByRole(Users.Role role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Users createUser(String username, String email, String password, Users.Role role);
    boolean changePassword(int userId, String oldPassword, String newPassword);
    void activateUser(int userId);
    void deactivateUser(int userId);
}
