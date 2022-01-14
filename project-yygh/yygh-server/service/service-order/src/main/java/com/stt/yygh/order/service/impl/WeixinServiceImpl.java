package com.stt.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.stt.yygh.common.exception.YyghException;
import com.stt.yygh.common.result.ResultCodeEnum;
import com.stt.yygh.enums.OrderStatusEnum;
import com.stt.yygh.enums.PaymentStatusEnum;
import com.stt.yygh.enums.PaymentTypeEnum;
import com.stt.yygh.enums.RefundStatusEnum;
import com.stt.yygh.model.order.OrderInfo;
import com.stt.yygh.model.order.PaymentInfo;
import com.stt.yygh.model.order.RefundInfo;
import com.stt.yygh.order.service.OrderService;
import com.stt.yygh.order.service.PaymentService;
import com.stt.yygh.order.service.RefundInfoService;
import com.stt.yygh.order.service.WeixinService;
import com.stt.yygh.order.util.ConstantPropertiesUtils;
import com.stt.yygh.order.util.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RefundInfoService refundInfoService;

    /**
     * 根据订单号下单，生成支付链接
     */
    @Override
    public Map createNative(Long orderId) {
        try {
            Map payMap = readCache(orderId.toString());
            if (!Objects.isNull(payMap)) {
                return payMap;
            }
            //根据id获取订单信息
            OrderInfo order = orderService.getById(orderId);
            // 保存交易记录
            paymentService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());
            //1. 设置微信参数
            Map<String, String> param = createWeixinParam(order);
            //2. 发送请求给微信
            Map<String, String> resultMap = sendToWeixin(param);
            //3、封装返回结果集
            return result(order, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    private Map readCache(String key) {
        // 实际在redis中存储的字符串为
        // "[\"java.util.HashMap\",{\"codeUrl\":\"weixin://wxpay/bizpayurl?pr=z6PwVSJzz\",\"orderId\":[\"java.lang.Long\",14],\"totalFee\":[\"java.math.BigDecimal\",100],\"resultCode\":\"SUCCESS\"}]"
        return (Map) redisTemplate.opsForValue().get(key);
    }

    private Map<String, String> createWeixinParam(OrderInfo order) {
        Map<String, String> re = new HashMap<>();
        re.put("appid", ConstantPropertiesUtils.APPID);
        re.put("mch_id", ConstantPropertiesUtils.PARTNER);
        re.put("out_trade_no", order.getOutTradeNo());
        re.put("nonce_str", WXPayUtil.generateNonceStr()); // 随机字符串
        re.put("body", order.getReserveDate() + "就诊" + order.getDepname());
        re.put("spbill_create_ip", "127.0.0.1");
        re.put("trade_type", "NATIVE");
        re.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");

        //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
        // 测试中使用1分钱进行测试
        re.put("total_fee", "1");
        return re;
    }

    private Map<String, String> sendToWeixin(Map<String, String> param) throws Exception {
        //HTTPClient来根据URL访问第三方接口并且传递参数
        HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        //client设置参数，对参数使用商户key进行加密
        client.setXmlParam(WXPayUtil.generateSignedXml(param, ConstantPropertiesUtils.PARTNERKEY));
        client.setHttps(true);
        client.post();
        //返回第三方的数据 client返回的是xml格式，需要转换为map
        return WXPayUtil.xmlToMap(client.getContent());
    }

    // 封装结果
    private Map<String, Object> result(OrderInfo order, Map<String, String> weixinResult) {
        Map<String, Object> re = new HashMap<>();
        re.put("orderId", order.getId());
        re.put("totalFee", order.getAmount());
        re.put("resultCode", weixinResult.get("result_code"));
        re.put("codeUrl", weixinResult.get("code_url")); // 二维码url

        if (!Objects.isNull(weixinResult.get("result_code"))) {
            // 微信支付二维码 2小时过期，可采取2小时未支付取消订单
            redisTemplate.opsForValue().set(order.getId().toString(), re, 120, TimeUnit.MINUTES);
        }
        return re;
    }

    @Override
    public Map queryPayStatus(Long orderId, String paymentType) {
        try {
            OrderInfo orderInfo = orderService.getById(orderId);
            //1、封装参数
            Map<String, String> param = new HashMap<>();
            param.put("appid", ConstantPropertiesUtils.APPID);
            param.put("mch_id", ConstantPropertiesUtils.PARTNER);
            param.put("out_trade_no", orderInfo.getOutTradeNo());
            param.put("nonce_str", WXPayUtil.generateNonceStr());
            //2、设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(param, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据，转成Map
            //4、返回
            return WXPayUtil.xmlToMap(client.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Boolean refund(Long orderId) {
        try {
            PaymentInfo payment = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            // 创建退款记录
            RefundInfo refund = refundInfoService.saveRefundInfo(payment);
            // 已退款则返回成功
            if (Objects.equals(refund.getRefundStatus(), RefundStatusEnum.REFUND.getStatus())) {
//                return true;
            }
            Map<String, String> param = createRefundParamForWeixin(payment);
            Map<String, String> result = refundByWeixin(param);
            if(Objects.isNull(result)) {
                return false;
            }
            // 更新 退款记录
            if (WXPayConstants.SUCCESS.equalsIgnoreCase(result.get("result_code"))) {
                refund.setCallbackTime(new Date());
                refund.setTradeNo(result.get("refund_id"));
                refund.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refund.setCallbackContent(JSONObject.toJSONString(result));
                refundInfoService.updateById(refund);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Map<String, String> createRefundParamForWeixin(PaymentInfo payment) {
        Map<String, String> re = new HashMap<>(8);
        re.put("appid", ConstantPropertiesUtils.APPID);       //公众账号ID
        re.put("mch_id", ConstantPropertiesUtils.PARTNER);    //商户编号
        re.put("nonce_str", WXPayUtil.generateNonceStr());
        re.put("transaction_id", payment.getTradeNo());   //微信订单号
        re.put("out_trade_no", payment.getOutTradeNo());  //商户订单编号
        re.put("out_refund_no", "tk" + payment.getOutTradeNo()); //商户退款单号

//      paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
//      paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");

        // 用于测试
        re.put("total_fee", "1");
        re.put("refund_fee", "1");
        return re;
    }

    private Map<String, String> refundByWeixin(Map<String, String> param) throws Exception {
        String xmlParam = WXPayUtil.generateSignedXml(param, ConstantPropertiesUtils.PARTNERKEY);
        HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
        client.setXmlParam(xmlParam);
        client.setHttps(true);
        // 注意：退款需要使用证书
        client.setCert(true);
        client.setCertPassword(ConstantPropertiesUtils.PARTNER);
        client.post();
        // 返回第三方的数据
        return WXPayUtil.xmlToMap(client.getContent());
    }

}
