package com.stt.yygh.hosp.controller;

import com.stt.yygh.common.result.Result;
import com.stt.yygh.hosp.service.DepartmentService;
import com.stt.yygh.hosp.service.HospitalService;
import com.stt.yygh.hosp.service.HospitalSetService;
import com.stt.yygh.hosp.service.ScheduleService;
import com.stt.yygh.model.hosp.Hospital;
import com.stt.yygh.vo.hosp.HospitalQueryVo;
import com.stt.yygh.vo.hosp.ScheduleOrderVo;
import com.stt.yygh.vo.hosp.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "医院管理接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {

    @Autowired
    private HospitalService service;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result page(@PathVariable Integer page,
                       @PathVariable Integer limit,
                       HospitalQueryVo hospitalQueryVo) {
        //显示上线的医院
        hospitalQueryVo.setStatus(1);
        Page<Hospital> re = service.selectPage(page, limit, hospitalQueryVo);
        return Result.ok(re);
    }

    @ApiOperation(value = "根据医院名称获取医院列表")

    @GetMapping("findByHosname/{hosname}")
    public Result findByHosname(@ApiParam(name = "hosname", value = "医院名称", required = true)
                                @PathVariable String hosname) {
        List<Hospital> re = service.findByHosname(hosname);
        return Result.ok(re);
    }

    @ApiOperation(value = "获取科室列表")
    @GetMapping("department/{hoscode}")
    public Result department(@ApiParam(name = "hoscode", value = "医院code", required = true)
                             @PathVariable String hoscode) {
        return Result.ok(departmentService.findDeptTree(hoscode));
    }

    @ApiOperation(value = "医院预约挂号详情")
    @GetMapping("{hoscode}")
    public Result detail(@ApiParam(name = "hoscode", value = "医院code", required = true)
                         @PathVariable String hoscode) {
        return Result.ok(service.detail(hoscode));
    }

    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(@PathVariable Integer page,
                                     @PathVariable Integer limit,
                                     @PathVariable String hoscode,
                                     @PathVariable String depcode) {
        return Result.ok(scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode));
    }

    // workDate 排班日期
    // depcode  科室code
    // hoscode  医院code
    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(@PathVariable String hoscode,
                                   @PathVariable String depcode,
                                   @PathVariable String workDate) {
        return Result.ok(scheduleService.getDetailSchedule(hoscode, depcode, workDate));
    }


    @ApiOperation(value = "根据排班id获取排班数据")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getSchedule(@ApiParam(name = "scheduleId", value = "排班id", required = true)
                              @PathVariable String scheduleId) {
        return Result.ok(scheduleService.getById(scheduleId));
    }

    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@ApiParam(name = "scheduleId", value = "排班id", required = true)
                                              @PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable("hoscode") String hoscode) {
        return hospitalSetService.getSignInfoVo(hoscode);
    }

}