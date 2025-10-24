package com.group7.DMS.service;

import com.group7.DMS.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByUsernameOrEmail(String username, String email);
    User save(User user);
    User update(User user);
    void delete(int id);
    List<User> findAll();
    List<User> findByRole(User.Role role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User createUser(String username, String email, String password, User.Role role);
    boolean changePassword(int userId, String oldPassword, String newPassword);
    void activateUser(int userId);
    void deactivateUser(int userId);
}
