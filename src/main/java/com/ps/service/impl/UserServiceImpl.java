package com.ps.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import cn.hutool.http.HttpUtil;
import com.ps.pojo.User;
import com.ps.service.UserService;
import com.ps.utils.JwtUtils;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WxMaService wxMaService;

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
        Query query1 = new Query(Criteria.where("user_id").is(openId).and("user_auth").isNull());
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
            String jwt = JwtUtils.generateJwt(claims);

            data.put("token",jwt);
            data.put("is_user_info_complete", false);

            return data;
        }else {

            claims.put("user_auth", res.getUser_auth());
            String jwt = JwtUtils.generateJwt(claims);

            data.put("token",jwt);
            if (res.getUser_auth() == null)
                data.put("is_user_info_complete", false);
            else data.put("is_user_info_complete", true);

            return data;
        }
    }

}
