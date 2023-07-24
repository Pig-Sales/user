package com.ps.controller;

import com.ps.pojo.Code;
import com.ps.pojo.Page;
import com.ps.pojo.Result;
import com.ps.pojo.User;
import com.ps.service.UserService;
import com.ps.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    public UserService userService;

    @PostMapping("/user/login")
    public Result login(@RequestBody Code code){
        return Result.success(userService.login(code.getCode()));
    }

    @PostMapping("/user/alterUserInfo")
    public Result alterUserInfo(@RequestBody User user){
        userService.alterUserInfo(user);
        return Result.success();
    }

    @PostMapping("/user/getUserInfoByToken")
    public Result getUserInfoByToken(@RequestHeader String jwt){
        Claims claims = JwtUtils.parseJWT(jwt);
        String openId = (String) claims.get("openId");
        return Result.success(userService.getUserInfoByToken(openId));
    }

    @PostMapping("/user/getAllApproving")
    public Result getAllApproving(@RequestBody Page page){
        return Result.success(userService.getAllApproving(page.getPage_size(), page.getPage_num()));
    }
}
