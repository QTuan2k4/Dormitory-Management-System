package com.group7.DMS.dao;

import com.group7.DMS.entity.User;

public interface UserDAO {
    User findByUsername(String username);
}
