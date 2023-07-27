package com.ps.controller;

import com.ps.pojo.*;
import com.ps.service.UserService;
import com.ps.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Value("${jwt.signKey}")
    private String signKey;

    @PostMapping("/user/login")
    public Result login(@RequestBody Code code){
        return Result.success(userService.login(code.getCode()));
    }

    @PostMapping("/user/alterUserInfo")
    public Result alterUserInfo(@RequestBody User user, @RequestHeader String Authorization){
        Claims claims = JwtUtils.parseJWT(Authorization, signKey);
        String openId = (String) claims.get("openId");
        String user_auth = (String) claims.get("user_auth");
        String jwt = userService.alterUserInfo(user, openId, user_auth);
        if (jwt != ""){
            Map<String,String> map = new HashMap<>();
            map.put("token", jwt);
            return Result.success(map);
        }
        return Result.success();
    }

    @PostMapping("/user/getUserInfoByToken")
    public Result getUserInfoByToken(@RequestHeader String Authorization){
        Claims claims = JwtUtils.parseJWT(Authorization, signKey);
        String openId = (String) claims.get("openId");
        return Result.success(userService.getUserInfoByToken(openId));
    }

    @PostMapping("/user/getAllApproving")
    public Result getAllApproving(@RequestBody Page page){
        return Result.success(userService.getAllApproving(page.getPage_size(), page.getPage_num()));
    }

    @PostMapping("/user/uploadImage")
    public Result uploadImage(MultipartFile image){
        Map<String,String> map = new HashMap<>();
        map.put("image_url",userService.uploadImage(image));
        return Result.success(map);
    }

    @PostMapping("/isSellerSafe")
    public Result isSellerSafe(String openId){
        return Result.success(userService.isSellerSafe(openId));
    }

    @PostMapping("/getUseridByName")
    public Result getUseridByName(String user_name){
        return Result.success(userService.getUseridByName(user_name));
    }



}
