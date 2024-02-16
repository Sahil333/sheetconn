package com.sheetconn.connector.controllers;

import com.sheetconn.connector.controllers.dto.AddUserDto;
import com.sheetconn.connector.service.UserService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
//    @PutMapping
//    public void addUser(@RequestBody AddUserDto addUserDto) {
//        userService.addUser(addUserDto.getIdToken());
//    }
}
