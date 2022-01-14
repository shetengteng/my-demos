package com.stt.yygh.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stt.yygh.common.result.Result;
import com.stt.yygh.user.service.UserInfoService;
import com.stt.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private UserInfoService service;

    //用户列表（条件查询带分页）
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       UserInfoQueryVo userInfoQueryVo) {
        return Result.ok(service.selectPage(new Page<>(page, limit), userInfoQueryVo));
    }

    @ApiOperation(value = "锁定")
    @GetMapping("lock/{userId}/{status}")
    public Result lock(@PathVariable("userId") Long userId,
                       @PathVariable("status") Integer status) {
        service.lock(userId, status);
        return Result.ok();
    }

    //用户详情
    @GetMapping("show/{userId}")
    public Result show(@PathVariable Long userId) {
        Map<String,Object> map = service.show(userId);
        return Result.ok(map);
    }

    //认证审批
    @GetMapping("approve/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId,@PathVariable Integer authStatus) {
        service.approve(userId,authStatus);
        return Result.ok();
    }
}
