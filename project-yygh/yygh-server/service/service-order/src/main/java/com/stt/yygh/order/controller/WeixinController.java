package com.stt.yygh.order.controller;

import com.stt.yygh.common.result.Result;
import com.stt.yygh.enums.PaymentTypeEnum;
import com.stt.yygh.order.service.PaymentService;
import com.stt.yygh.order.service.WeixinService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {

    @Autowired
    private WeixinService service;

    @Autowired
    private PaymentService paymentService;

    /**
     * 下单 生成二维码
     */
    @GetMapping("/createNative/{orderId}")
    public Result createNative(@ApiParam(name = "orderId", value = "订单id", required = true)
                               @PathVariable("orderId") Long orderId) {
        return Result.ok(service.createNative(orderId));
    }

    @ApiOperation(value = "查询支付状态")
    @GetMapping("/queryPayStatus/{orderId}")
    public Result queryPayStatus(@ApiParam(name = "orderId", value = "订单id", required = true)
                                 @PathVariable("orderId") Long orderId) {
        //调用查询接口
        Map<String, String> resultMap = service.queryPayStatus(orderId, PaymentTypeEnum.WEIXIN.name());
        if (Objects.isNull(resultMap)) {
            return Result.fail().message("支付出错");
        }
        //如果成功
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {
            //更改订单状态，处理支付结果
            String out_trade_no = resultMap.get("out_trade_no");
            paymentService.paySuccess(out_trade_no, PaymentTypeEnum.WEIXIN.getStatus(), resultMap);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }

}
