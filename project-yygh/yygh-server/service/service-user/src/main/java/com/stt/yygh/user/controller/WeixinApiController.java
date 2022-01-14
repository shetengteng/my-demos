package com.stt.yygh.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stt.yygh.common.exception.YyghException;
import com.stt.yygh.common.helper.JwtHelper;
import com.stt.yygh.common.result.Result;
import com.stt.yygh.common.result.ResultCodeEnum;
import com.stt.yygh.model.user.UserInfo;
import com.stt.yygh.user.config.ConstantPropertiesUtil;
import com.stt.yygh.user.service.UserInfoService;
import com.stt.yygh.user.utils.HttpClientUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/api/ucenter/wx")
public class WeixinApiController {

    @Autowired
    private UserInfoService userInfoService;

    //  获取微信登录参数
    // scope：应用授权作用域，拥有多个作用域用逗号（,）分隔，网页应用目前仅填写snsapi_login
    // state：用于保持请求和回调的状态，授权请求后原样带回给第三方。
    //       该参数可用于防止csrf攻击（跨站请求伪造攻击），
    //      建议第三方带上该参数，可设置为简单的随机数加session进行校验
    @GetMapping("getLoginParam")
    @ResponseBody
    public Result getQRConnect() throws UnsupportedEncodingException {
        Map<String, Object> re = new HashMap<>(4);
        re.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        re.put("redirectUri", URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, Charsets.UTF_8.name()));
        re.put("scope", "snsapi_login");
        re.put("state", System.currentTimeMillis() + "");
        return Result.ok(re);
    }

    // 微信登录回调 传递code，服务通过code再访问微信获取用户名称等信息
    @RequestMapping("callback")
    public String callback(String code, String state) throws UnsupportedEncodingException {
        System.out.println("微信授权服务器回调...");
        System.out.println("state = " + state);
        System.out.println("code = " + code);
        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(code)) {
            log.error("非法回调请求");
            throw new YyghException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }
        WxReturnInfo accessInfo = this.getUserInfoAccessInfo(code);

        // 从本地数据库查看是否存在，存在则直接返回，不存在则存储
        UserInfo userInfo = userInfoService.getByOpenid(accessInfo.openId);
        if (Objects.isNull(userInfo)) {
            userInfo = this.getUserInfo(accessInfo);
            userInfoService.save(userInfo);
        }
        return "redirect:" + getReturnUrl(userInfo);
    }

    // 返回重定向路径
    private String getReturnUrl(UserInfo userInfo) throws UnsupportedEncodingException {
        String token = JwtHelper.createToken(userInfo.getId(), userInfo.getName());
        Boolean hasPhone = !StringUtils.isEmpty(userInfo.getPhone());
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        // 重定向到weixin/callback页面读取用户名称和openid
        return String.format(ConstantPropertiesUtil.YYGH_BASE_URL + "/weixin/callback?token=%s&openid=%s&name=%s&hasPhone=%b",
                token, userInfo.getOpenid(), URLEncoder.encode(name, Charsets.UTF_8.name()), hasPhone);
    }

    private UserInfo getUserInfo(WxReturnInfo accessInfo) {
        String url = String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s",
                accessInfo.accessToken, accessInfo.openId);

        String resultUserInfo;
        try {
            resultUserInfo = HttpClientUtils.get(url);
            System.out.println("使用access_token获取用户信息的结果 = " + resultUserInfo);
        } catch (Exception e) {
            throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
        }

        JSONObject user = JSONObject.parseObject(resultUserInfo);
        if (!StringUtils.isEmpty(user.getString("errcode"))) {
            log.error("获取用户信息失败：" + user.getString("errcode") + user.getString("errmsg"));
            throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
        }
        //解析用户信息
        String nickname = user.getString("nickname");
//        String headimgurl = user.getString("headimgurl"); // 头像url

        UserInfo userInfo = new UserInfo();
        userInfo.setOpenid(accessInfo.openId);
        userInfo.setNickName(nickname);
        userInfo.setStatus(1);
        return userInfo;
    }

    // 通过code获取用户信息的微信 url
    private WxReturnInfo getUserInfoAccessInfo(String code) {
        // 拼接访问微信获取用户信息url
        String accessTokenUrl = String.format("https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        String result;
        try {
            result = HttpClientUtils.get(accessTokenUrl);
            System.out.println("使用code换取的access_token结果 = " + result);
        } catch (Exception e) {
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        // 解析结果获取access_token和openid，去拉取用户信息
        JSONObject resultJson = JSONObject.parseObject(result);
        if (!StringUtils.isEmpty(resultJson.getString("errcode"))) {
            log.error("获取access_token失败：" + resultJson.getString("errcode") + resultJson.getString("errmsg"));
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }
        String accessToken = resultJson.getString("access_token");
        String openId = resultJson.getString("openid");
        log.info(accessToken);
        log.info(openId);

        return new WxReturnInfo(accessToken, openId);

    }

    @Data
    @AllArgsConstructor
    class WxReturnInfo {
        private String accessToken;
        private String openId;
    }


}
