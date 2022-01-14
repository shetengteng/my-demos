package com.stt.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stt.yygh.common.result.Result;
import com.stt.yygh.common.utils.MD5;
import com.stt.yygh.hosp.service.HospitalSetService;
import com.stt.yygh.model.hosp.HospitalSet;
import com.stt.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
// 允许跨域访问 使用gateway的全局配置
//@CrossOrigin
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;

    //1 查询医院设置表所有信息
    @ApiOperation(value = "获取所有医院设置")
    @GetMapping("findAll")
    public Result findAll() {
//        try {
//            int i = 1 / 0;
//        } catch (Exception e) {
//            throw new YyghException("error", 400);
//        }
        List<HospitalSet> re = hospitalSetService.list();
        return Result.ok(re);
    }

    //2 逻辑删除医院设置
    @ApiOperation(value = "逻辑删除医院设置")
    @DeleteMapping("{id}")
    public Result remove(@PathVariable Long id) {
        boolean re = hospitalSetService.removeById(id);
        return re ? Result.ok() : Result.fail();
    }

    //3 条件查询带分页
    @PostMapping("findPageHospSet/{current}/{limit}")
    public Result findPage(@PathVariable long current,
                           @PathVariable long limit,
                           @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo) {
        // 构建查询条件
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(hospitalSetQueryVo.getHosname())) {
            queryWrapper.like("hosname", hospitalSetQueryVo.getHosname());
        }
        if (!StringUtils.isEmpty(hospitalSetQueryVo.getHoscode())) {
            queryWrapper.like("hoscode", hospitalSetQueryVo.getHoscode());
        }
        // 查询分页
        Page<HospitalSet> re = hospitalSetService.page(new Page<>(current, limit), queryWrapper);
        return Result.ok(re);
    }

    //4 添加医院设置
    @PostMapping("saveHospitalSet")
    public Result save(@RequestBody HospitalSet hospitalSet) {
        hospitalSet.setStatus(1); // 1 启用， 0 禁用
        // 随机数签名
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000)));
        boolean re = hospitalSetService.save(hospitalSet);
        if (re) {
            return Result.ok();
        }
        return Result.fail();
    }

    //5 根据id获取医院设置
    @GetMapping("getHospSet/{id}")
    public Result getById(@PathVariable Long id) {
        HospitalSet re = hospitalSetService.getById(id);
        return Result.ok(re);
    }

    //6 修改医院设置
    @PostMapping("updateHospitalSet")
    public Result update(@RequestBody HospitalSet hospitalSet) {
        boolean re = hospitalSetService.updateById(hospitalSet);
        if (re) {
            return Result.ok();
        }
        return Result.fail();
    }

    //7 批量删除医院设置
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList) {
        hospitalSetService.removeByIds(idList);
        return Result.ok();
    }

    //8 医院设置锁定和解锁
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result updateStatus(@PathVariable Long id,
                               @PathVariable Integer status) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        boolean re = hospitalSetService.updateById(hospitalSet);
        if (re) {
            return Result.ok();
        }
        return Result.fail();
    }

    //9 发送签名秘钥
    @PutMapping("sendKey/{id}")
    public Result lockHospitalSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hosCode = hospitalSet.getHoscode();
        // todo 发送短信
        return Result.ok();
    }


}
