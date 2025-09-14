package com.shippix.Payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig
{
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder)
    {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        //admin
        manager.createUser(
                User.withUsername("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .roles("ADMIN")
                        .build()
        );

        //business owner
        manager.createUser(
                User.withUsername("owner")
                        .password(passwordEncoder.encode("owner123"))
                        .roles("BUSINESS_OWNER")
                        .build()
        );

        //mock gateway
        manager.createUser(
                User.withUsername("gateway")
                        .password(passwordEncoder.encode("gateway123"))
                        .roles("GATEWAY")
                        .build()
        );

        return manager;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/payments/all").hasRole("ADMIN")

                        .requestMatchers("/api/payments/create").hasRole("BUSINESS_OWNER")
                        .requestMatchers("/api/payments/{orderId}").hasRole("BUSINESS_OWNER")

                        .requestMatchers("/api/payments/simulate/**").hasRole("GATEWAY")
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {});

        return http.build();
    }
}
