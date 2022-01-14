package com.stt.yygh.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stt.yygh.model.order.OrderInfo;
import com.stt.yygh.vo.order.OrderCountQueryVo;
import com.stt.yygh.vo.order.OrderQueryVo;

import java.util.Map;

public interface OrderService extends IService<OrderInfo> {
    //保存订单
    Long saveOrder(String scheduleId, Long patientId);

    // 分页列表
    Page<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    // 获取订单详情
    OrderInfo getOrder(Long id);

    // 订单详情 给后台系统使用
    Map<String, Object> show(Long orderId);

    // 取消订单
    Boolean cancelOrder(Long orderId);

    // 就诊提醒
    void patientTips();

    // 订单统计
    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
}