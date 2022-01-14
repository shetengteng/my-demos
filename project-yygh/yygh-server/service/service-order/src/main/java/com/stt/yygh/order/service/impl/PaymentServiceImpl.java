package com.stt.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stt.yygh.common.exception.YyghException;
import com.stt.yygh.common.helper.HttpRequestHelper;
import com.stt.yygh.common.result.ResultCodeEnum;
import com.stt.yygh.enums.OrderStatusEnum;
import com.stt.yygh.enums.PaymentStatusEnum;
import com.stt.yygh.hosp.client.HospitalFeignClient;
import com.stt.yygh.model.order.OrderInfo;
import com.stt.yygh.model.order.PaymentInfo;
import com.stt.yygh.order.mapper.PaymentInfoMapper;
import com.stt.yygh.order.service.OrderService;
import com.stt.yygh.order.service.PaymentService;
import com.stt.yygh.vo.hosp.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    /**
     * 保存交易记录
     *
     * @param orderInfo
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        // 如果支付订单已经存在，则不做处理
        if (paymentExisted(orderInfo.getId(), paymentType)) {
            return;
        }
        // 保存交易记录
        PaymentInfo paymentInfo = createPayment(orderInfo, paymentType);
        baseMapper.insert(paymentInfo);
    }

    private boolean paymentExisted(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("payment_type", paymentType);
        Integer count = baseMapper.selectCount(queryWrapper);
        return count > 0;
    }

    private PaymentInfo createPayment(OrderInfo order, Integer paymentType) {
        PaymentInfo re = new PaymentInfo();
        re.setCreateTime(new Date());
        re.setOrderId(order.getId());
        re.setPaymentType(paymentType);
        re.setOutTradeNo(order.getOutTradeNo());
        re.setTotalAmount(order.getAmount());
        re.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        re.setSubject(new DateTime(order.getReserveDate()).toString("yyyy-MM-dd") + "|" +
                order.getHosname() + "|" +
                order.getDepname() + "|" +
                order.getTitle());
        return re;
    }

    /**
     * 支付成功
     */
    @Override
    public void paySuccess(String outTradeNo, Integer paymentType, Map<String, String> paramMap) {
        PaymentInfo paymentInfo = this.getPaymentInfo(outTradeNo, paymentType);
        if (Objects.isNull(paymentInfo)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        if (!Objects.equals(paymentInfo.getPaymentStatus(), PaymentStatusEnum.UNPAID.getStatus())) {
            return;
        }

        //修改支付状态
        PaymentInfo paymentInfoUpd = new PaymentInfo();
        paymentInfoUpd.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfoUpd.setTradeNo(paramMap.get("transaction_id"));
        paymentInfoUpd.setCallbackTime(new Date());
        paymentInfoUpd.setCallbackContent(paramMap.toString());

        this.updatePaymentInfo(outTradeNo, paymentInfoUpd);

        //修改订单状态
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);

        // 调用医院接口，通知更新支付状态
        updatePayStatusForRemoteHospSystem(orderInfo);
    }

    private void updatePayStatusForRemoteHospSystem(OrderInfo order){
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(order.getHoscode());
        if(Objects.isNull(signInfoVo)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 调用远端接口，更新医院模拟系统数据
        Map<String, Object> param = new HashMap<>();
        param.put("hoscode",order.getHoscode());
        param.put("hosRecordId",order.getHosRecordId());
        param.put("timestamp", HttpRequestHelper.getTimestamp());
        param.put("sign", HttpRequestHelper.getSign(param, signInfoVo.getSignKey()));
        JSONObject result = HttpRequestHelper.sendRequest(param, signInfoVo.getApiUrl()+"/order/updatePayStatus");
        if(result.getInteger("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
    }
    /**
     * 获取支付记录
     */
    private PaymentInfo getPaymentInfo(String outTradeNo, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", outTradeNo);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 更改支付记录
     */
    private void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfoUpd) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", outTradeNo);
        baseMapper.update(paymentInfoUpd, queryWrapper);
    }

}
