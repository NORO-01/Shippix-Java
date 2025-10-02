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
	CommandLineRunner initAdminsAndGateway(UserRepo userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Admin
			var existingAdmin = userRepository.findByUsername("admin1");
			if (existingAdmin.isEmpty()) {
				Users admin = new Users();
				admin.setUsername("admin1");
				admin.setEmail("nouredinshimi@gmail.com");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setRole(Users.Role.ROLE_ADMIN);
				userRepository.save(admin);
				System.out.println("Admin user created: admin1 / admin123");
			} else {
				Users admin = existingAdmin.get();
				admin.setEmail("nouredinshimi@gmail.com");
				admin.setPassword(passwordEncoder.encode("admin123"));
				admin.setRole(Users.Role.ROLE_ADMIN);
				userRepository.save(admin);
				System.out.println("Admin user updated: admin1 / admin123");
			}

			// Gateway
			var existingGateway = userRepository.findByUsername("gateway");
			if (existingGateway.isEmpty()) {
				Users gateway = new Users();
				gateway.setUsername("gateway");
				gateway.setEmail("gateway@shippix.com");
				gateway.setPassword(passwordEncoder.encode("gateway123"));
				gateway.setRole(Users.Role.ROLE_GATEWAY);
				userRepository.save(gateway);
				System.out.println("Gateway user created: gateway / gateway123");
			} else {
				Users gateway = existingGateway.get();
				gateway.setEmail("gateway@shippix.com");
				gateway.setPassword(passwordEncoder.encode("gateway123"));
				gateway.setRole(Users.Role.ROLE_GATEWAY);
				userRepository.save(gateway);
				System.out.println("Gateway user updated: gateway / gateway123");
			}
		};
	}


}
