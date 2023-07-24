package com.ps.controller;

import com.ps.pojo.Result;
import com.ps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    public UserService userService;

    @PostMapping("/user/login")
    public Result login(@RequestParam String code){
        return Result.success(userService.login(code));
    }
}
