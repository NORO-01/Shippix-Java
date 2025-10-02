package com.shippix.User_Management.Service;

import com.shippix.User_Management.Model.UserPrincipal;
import com.shippix.User_Management.Model.Users;
import com.shippix.User_Management.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users users = userRepo.findByEmail(email)
                .orElseGet(() -> userRepo.findByUsername(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email/username: " + email)));

        return new UserPrincipal(users);
    }

    // Optional: keep this if you sometimes want to fetch by username manually
    public UserDetails loadUserByAppUsername(String username) throws UsernameNotFoundException {
        Users users = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new UserPrincipal(users);
    }
}
