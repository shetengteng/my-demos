package com.stt.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stt.yygh.enums.RefundStatusEnum;
import com.stt.yygh.model.order.PaymentInfo;
import com.stt.yygh.model.order.RefundInfo;
import com.stt.yygh.order.mapper.RefundInfoMapper;
import com.stt.yygh.order.service.RefundInfoService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        RefundInfo re = this.getRefundInfo(paymentInfo);
        if(!Objects.isNull(re)){
            return re;
        }

        // 保存交易记录
        re = new RefundInfo();
        re.setCreateTime(new Date());
        re.setOrderId(paymentInfo.getOrderId());
        re.setPaymentType(paymentInfo.getPaymentType());
        re.setOutTradeNo(paymentInfo.getOutTradeNo());
        re.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        re.setSubject(paymentInfo.getSubject());
        re.setTotalAmount(paymentInfo.getTotalAmount());
        baseMapper.insert(re);
        return re;
    }

    private RefundInfo getRefundInfo(PaymentInfo paymentInfo){
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", paymentInfo.getOrderId());
        queryWrapper.eq("payment_type", paymentInfo.getPaymentType());
        return baseMapper.selectOne(queryWrapper);
    }
}
