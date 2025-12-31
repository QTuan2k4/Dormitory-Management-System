package com.group7.DMS.repository;

import com.group7.DMS.entity.Users;
import com.group7.DMS.entity.Users.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
	Optional<Users> findByUsername(String username);

	Optional<Users> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	List<Users> findByRoleInAndActiveTrue(List<Users.Role> roles);

	@Query("SELECT u FROM Users u WHERE u.username = :username OR u.email = :email")
	Optional<Users> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
}
