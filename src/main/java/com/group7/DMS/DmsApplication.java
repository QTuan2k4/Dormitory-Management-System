package com.group7.DMS;

import com.group7.DMS.entity.Users;
import com.group7.DMS.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DmsApplication {

	private static final Logger log = LoggerFactory.getLogger(DmsApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DmsApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedDefaultAdmin(UserService userService) {
		return args -> {
			final String defaultUsername = "admin";
			if (!userService.existsByUsername(defaultUsername)) {
				userService.createUser(defaultUsername, "admin@gmail.com", "admin123", Users.Role.ADMIN);
				log.info("Default admin user created with username '{}'.", defaultUsername);
			} else {
				log.debug("Default admin user '{}' already exists. Skipping seeding.", defaultUsername);
			}
		};
	}
}
