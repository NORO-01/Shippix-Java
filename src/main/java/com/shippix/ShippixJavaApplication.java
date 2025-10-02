package com.shippix;

import com.shippix.User_Management.Model.BusinessOwner;
import com.shippix.User_Management.Model.BusinessType;
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
			if(userRepository.findByUsername("owner1").isEmpty()) {
				BusinessOwner owner = new BusinessOwner();
				owner.setUsername("owner1");
				owner.setPassword(passwordEncoder.encode("owner123"));
				owner.setEmail("owner1@example.com");
				owner.setRole(Users.Role.ROLE_BUSINESS_OWNER);
				owner.setBusinessName("Tech Store");
				owner.setBusinessType(BusinessType.ELECTRONICS_STORE);
				owner.setLatitude(30.0444); // Cairo example
				owner.setLongitude(31.2357); 
				userRepository.save(owner);
				System.out.println("Business owner created: " + owner.getUsername() + " / owner123");
			}
		};
	}

}