package com.shippix.User_Management.Service;


import com.shippix.User_Management.Model.Users;
import com.shippix.User_Management.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

//    private final JwtService jwtService;
//    private final AuthenticationManager authManager;
//    private final UserRepo repo;
//    private final BCryptPasswordEncoder encoder;
//
//    public Users register(Users user) {
//        user.setPassword(encoder.encode(user.getPassword()));
//        repo.save(user);
//        return user;
//    }
//
//    public String verify(Users user) {
//        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
//        if (authentication.isAuthenticated()) {
//            return jwtService.generateToken(user.getUsername());
//        } else {
//            return "fail";
//        }
//    }
}