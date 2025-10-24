package com.group7.DMS.dao;

import com.group7.DMS.entity.Users;

public interface UserDAO {
    Users findByUsername(String username);
}
