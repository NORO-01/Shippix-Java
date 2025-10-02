package com.shippix.config;
import com.shippix.User_Management.Config.JwtFilter;
import com.shippix.User_Management.Service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AppSecurityConfig
{
    private final JwtFilter jwtFilter;
    private final MyUserDetailsService myUserDetailsService;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/business-types/**").permitAll()

                        // User management
                        .requestMatchers("/api/users/me").permitAll()
                        .requestMatchers("/api/users/**").authenticated()

                        // Order Request endpoints
                        .requestMatchers("/api/order-requests/**").hasRole("BUSINESS_OWNER")
                        .requestMatchers("/api/orders/my-orders").hasRole("BUSINESS_OWNER")
                        .requestMatchers("/api/orders/{id}").hasRole("BUSINESS_OWNER")
                        .requestMatchers("/api/orders/{id}/feedback").hasRole("BUSINESS_OWNER")

                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Payment endpoints
                        .requestMatchers("/api/payments/create/**").hasRole("BUSINESS_OWNER")
                        .requestMatchers("/api/payments/{paymentId}/validate/**").hasRole("GATEWAY")

                        // Pricing endpoint
                        .requestMatchers("/api/pricing/**").hasAnyRole("ADMIN", "BUSINESS_OWNER")

                        //dashboard
                        .requestMatchers("/api/analytics/**").hasRole("BUSINESS_OWNER")

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider(BCryptPasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(myUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }





}


















