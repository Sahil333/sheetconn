package com.sheetconn.connector.service;

import com.sheetconn.connector.model.User;
import com.sheetconn.connector.oauth.jwt.GoogleIdTokenVerifier;
import com.sheetconn.connector.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GoogleIdTokenVerifier idTokenVerifier;

    public UserService(UserRepository userRepository, GoogleIdTokenVerifier idTokenVerifier) {
        this.userRepository = userRepository;
        this.idTokenVerifier = idTokenVerifier;
    }

    public void addUser(Claims claims) {
        if (claims == null) {
            throw new RuntimeException("Claims can not be null");
        }

        boolean isUserExists = userRepository.existsById(claims.getSubject());

        if(isUserExists) return;

        User user = new User(
                claims.getSubject(),
                claims.get("name", String.class),
                claims.get("email", String.class),
                claims.get("picture", String.class)
        );

        userRepository.save(user);
    }


}
