package com.ps.service;

import com.ps.pojo.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    Map login(String code);

    void alterUserInfo(User user);

    User getUserInfoByToken(String openId);

    List<User> getAllApproving(Integer pageSize, Integer pageNum);
}
