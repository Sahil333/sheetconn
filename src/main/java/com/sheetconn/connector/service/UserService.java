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

    public void addUser(String idToken) {
        // check for audience for appscript or other project client id
        // 1048130131806-f0pbb31sj8cduou6b9jldnmqspl2sabd.apps.googleusercontent.com - AppScripts
        Claims claims = idTokenVerifier.getClaims(idToken);

        if (claims == null) {
            throw new RuntimeException("Access token verification failed");
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
