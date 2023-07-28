package com.ps.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.github.pagehelper.PageHelper;
import com.ps.pojo.User;
import com.ps.service.UserService;
import com.ps.utils.AliOSSUtils;
import com.ps.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    AliOSSUtils aliOSSUtils;

    @Value("${jwt.signKey}")
    private String signKey;
    @Value("${jwt.expire}")
    private Long expire;

    @Override
    public Map login(String code) {
        Map<String, Object> data = new HashMap<>();
        String openId;

        try {
            WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
            openId = session.getOpenid();
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }

        Query query = new Query(Criteria.where("user_id").is(openId));
        Map<String, Object> claims = new HashMap<>();
        claims.put("openId", openId);
        User res = mongoTemplate.findOne(query, User.class,"user");
        if (res == null){
            User user = new User();
            user.setUser_id(openId);
            user.setCreate_time(LocalDateTime.now().toString());
            user.setUpdate_time(LocalDateTime.now().toString());
            mongoTemplate.insert(user,"user");

            claims.put("user_auth", null);
            String jwt = JwtUtils.generateJwt(claims, signKey, expire);

            data.put("token",jwt);
            data.put("is_user_info_complete", false);

        }else {

            claims.put("user_auth", res.getUser_auth());
            String jwt = JwtUtils.generateJwt(claims, signKey, expire);

            data.put("token",jwt);
            if (res.getUser_auth() == null)
                data.put("is_user_info_complete", false);
            else data.put("is_user_info_complete", true);

        }
        return data;
    }

    @Override
    public String alterUserInfo(User user, String openId, String user_auth) {
        Update update = new Update();
        Query query;
        String jwt = "";
        user.setUpdate_time(LocalDateTime.now().toString());
        if(Objects.equals(user_auth,"admin")) {
            query = new Query(Criteria.where("user_id").is(user.getUser_id()));
            try {
                if(user.getUser_auth() != null && Objects.equals(openId,user.getUser_id())){
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("openId", openId);
                    claims.put("user_auth", user.getUser_auth());
                    jwt = JwtUtils.generateJwt(claims, signKey, expire);
                }
                Class cls = user.getClass();
                Field[] fields = cls.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    f.setAccessible(true);
                    if (f.getName() != "user_id" && f.get(user) != "" && f.get(user) != null) {
                        update.set(f.getName(), f.get(user));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            query = new Query(Criteria.where("user_id").is(openId));
            try {
                if(user.getUser_auth() != null){
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("openId", openId);
                    claims.put("user_auth", user.getUser_auth());
                    jwt = JwtUtils.generateJwt(claims, signKey, expire);
                }
                Class cls = user.getClass();
                Field[] fields = cls.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field f = fields[i];
                    f.setAccessible(true);
                    if (f.getName() != "user_id" && f.getName() != "approved" && f.get(user) != "" && f.get(user) != null) {
                        update.set(f.getName(), f.get(user));
                    }else if (Objects.equals(f.getName(),"approved") && Objects.equals(f.get(user),"审核中")) {
                        update.set(f.getName(), f.get(user));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mongoTemplate.updateFirst(query, update, User.class, "user");
        return jwt;
    }




    @Override
    public User getUserInfoByToken(String openId) {
        Query query = new Query(Criteria.where("user_id").is(openId));
        User user = mongoTemplate.findOne(query, User.class, "user");
        return user;
    }

    @Override
    public List<User> getAllApproving(Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);

        Query query = new Query(Criteria.where("approved").is("审核中"));
        List<User> users = mongoTemplate.find(query, User.class, "user");
        return users;
    }

    @Override
    public boolean isSellerSafe(String openId) {
        Query query = new Query(Criteria.where("user_id").is(openId));
        User user = mongoTemplate.findOne(query, User.class, "user");
        if(user.getApproved() == "审核通过")
            return true;
        return false;
    }

    @Override
    public List<User> getUseridByName(String userName) {
        Pattern pattern = Pattern.compile("^.*"+userName+".*$", Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("username").regex(pattern));
        List<User> users = mongoTemplate.find(query, User.class, "user");
        return users;
    }

    @Override
    public String uploadImage(MultipartFile image) {
        String url;
        try {
            url = aliOSSUtils.upload(image, "image");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("上传文件：{}", image.getOriginalFilename());
        log.info("URL：{}", url);
        return url;
    }

}
