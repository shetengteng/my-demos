package com.stt.yygh.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stt.yygh.common.result.Result;
import com.stt.yygh.enums.OrderStatusEnum;
import com.stt.yygh.model.order.OrderInfo;
import com.stt.yygh.order.service.OrderService;
import com.stt.yygh.vo.order.OrderCountQueryVo;
import com.stt.yygh.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "订单接口")
@RestController
@RequestMapping("/admin/order/orderInfo")
public class OrderController {

    @Autowired
    private OrderService service;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(@ApiParam(name = "page", value = "当前页码", required = true) @PathVariable Long page,
                        @ApiParam(name = "limit", value = "每页记录数", required = true) @PathVariable Long limit,
                        @ApiParam(name = "orderCountQueryVo", value = "查询对象") OrderQueryVo orderQueryVo) {
        Page<OrderInfo> pageModel = service.selectPage(new Page<>(page, limit), orderQueryVo);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取订单状态")
    @GetMapping("getStatusList")
    public Result getStatusList() {
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    @ApiOperation(value = "获取订单")
    @GetMapping("show/{id}")
    public Result get(@ApiParam(name = "orderId", value = "订单id", required = true) @PathVariable Long id) {
        return Result.ok(service.show(id));
    }
}
