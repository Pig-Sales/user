package com.ps.service;

import com.ps.pojo.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    Map login(String code);

    String alterUserInfo(User user, String openId, String user_auth);

    User getUserInfoByToken(String openId, String phone);

    List<User> getAllApproving(Integer pageSize, Integer pageNum);

    boolean isSellerSafe(String openId);

    List<User> getUseridByName(String userName);

    String uploadImage(MultipartFile image);

    User getUserInfoById(String userId);

    Map loginByPassword(User user);
}
