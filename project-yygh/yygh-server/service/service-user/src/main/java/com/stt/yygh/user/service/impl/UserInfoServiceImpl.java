package com.stt.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stt.yygh.common.exception.YyghException;
import com.stt.yygh.common.helper.JwtHelper;
import com.stt.yygh.common.result.ResultCodeEnum;
import com.stt.yygh.enums.AuthStatusEnum;
import com.stt.yygh.model.user.UserInfo;
import com.stt.yygh.user.mapper.UserInfoMapper;
import com.stt.yygh.user.service.PatientService;
import com.stt.yygh.user.service.UserInfoService;
import com.stt.yygh.vo.user.LoginVo;
import com.stt.yygh.vo.user.UserAuthVo;
import com.stt.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PatientService patientService;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //校验参数
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        // 校验校验验证码
        String phoneCode = redisTemplate.opsForValue().get(phone);
        if (!Objects.equals(phoneCode, code)) {
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }
        UserInfo userInfo;
        if (!StringUtils.isEmpty(loginVo.getOpenid())) {
            // 通过openid判断是否存在，存在则更新手机号
            userInfo = getByOpenid(loginVo.getOpenid());
            if (Objects.isNull(userInfo)) {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
            userInfo.setPhone(loginVo.getPhone());
            // 更新userInfo
            this.updateById(userInfo);
        } else {
            // 通过手机号获取 会员，如果存在则返回，如果不存在则创建
            userInfo = saveUserInfoIfNotExistsByPhone(phone);
        }

        String name = getUserInfoName(userInfo);
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        // jwt生成token字符串
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        return map;
    }

    @Override
    public UserInfo getByOpenid(String openId) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("openid", openId);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //设置认证信息
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //进行信息更新
        baseMapper.updateById(userInfo);
    }

    @Override
    public Page<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        //UserInfoQueryVo获取条件值
        String name = userInfoQueryVo.getKeyword(); //用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间
        //对条件值进行非空判断
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            wrapper.like("name", name);
        }
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("status", status);
        }
        if (!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status", authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time", createTimeEnd);
        }
        //调用mapper的方法
        Page<UserInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        //编号变成对应值封装
        pages.getRecords().forEach(this::packageUserInfo);
        return pages;
    }

    //编号变成对应值封装
    private UserInfo packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理用户状态 0  1
        userInfo.getParam().put("statusString", userInfo.getStatus() == 0 ? "锁定" : "正常");
        return userInfo;
    }

    //返回页面显示名称，如果存在名称则返回，某则返回昵称，否则返回手机号作为名称
    private String getUserInfoName(UserInfo userInfo) {
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        return name;
    }

    // 通过手机号创建UserInfo，如果存在则返回，如果不存在则创建
    private UserInfo saveUserInfoIfNotExistsByPhone(String phone) {
        //手机号已被使用
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone);
        //获取会员
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        //第一次使用这个手机号登录
        if (Objects.isNull(userInfo)) {
            //添加信息到数据库
            userInfo = new UserInfo();
            userInfo.setName("");
            userInfo.setPhone(phone);
            userInfo.setStatus(1);
            this.save(userInfo);
        }
        //校验是否被禁用
        if (userInfo.getStatus() == 0) {
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }
        return userInfo;
    }

    @Override
    public void lock(Long userId, Integer status) {
        if (status == 0 || status == 1) {
            UserInfo userInfo = this.getById(userId);
            userInfo.setStatus(status);
            this.updateById(userInfo);
        }
    }


    //用户详情
    @Override
    public Map<String, Object> show(Long userId) {
        Map<String, Object> map = new HashMap<>();
        //根据userid查询用户信息
        map.put("userInfo", this.packageUserInfo(baseMapper.selectById(userId)));
        //根据userid查询就诊人信息
        map.put("patientList", patientService.findAllUserId(userId));
        return map;
    }

    // 认证审批  2通过  -1不通过
    @Override
    public void approve(Long userId, Integer authStatus) {
        if(authStatus == 2 || authStatus == -1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }
}
