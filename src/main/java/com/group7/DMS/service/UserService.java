package com.group7.DMS.service;

import com.group7.DMS.entity.User;

public interface UserService {
    User login(String username, String password);
}
