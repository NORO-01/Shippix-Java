package com.shippix;

import com.shippix.User_Management.Model.Users;
import com.shippix.User_Management.Repo.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ShippixJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShippixJavaApplication.class, args);
	}

	@Bean
	CommandLineRunner initAdmins(UserRepo userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			var existing = userRepository.findByUsername("admin1");
			if (existing.isEmpty()) {
				Users admin = new Users();
				admin.setUsername("admin1");
				admin.setEmail("nouredinshimi@gmail.com");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setRole(Users.Role.ROLE_ADMIN);
				userRepository.save(admin);
				System.out.println("Admin user created: admin1 / admin123");
			} else {
				Users admin = existing.get();
				admin.setEmail("nouredinshimi@gmail.com");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setRole(Users.Role.ROLE_ADMIN);
				userRepository.save(admin);
				System.out.println("Admin user updated: admin1 / admin123");
			}
		};
	}

}
