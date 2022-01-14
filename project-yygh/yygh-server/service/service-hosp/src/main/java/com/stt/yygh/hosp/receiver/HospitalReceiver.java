package com.stt.yygh.hosp.receiver;

import com.rabbitmq.client.Channel;
import com.stt.yygh.hosp.service.ScheduleService;
import com.stt.yygh.model.hosp.Schedule;
import com.stt.yygh.rabbitmq.MqConst;
import com.stt.yygh.rabbitmq.RabbitService;
import com.stt.yygh.vo.order.OrderMqVo;
import com.stt.yygh.vo.msm.MsmVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        Schedule schedule = scheduleService.getById(orderMqVo.getScheduleId());
        if (Objects.isNull(orderMqVo.getAvailableNumber())) {
            //取消预约 更新预约数
            schedule.setAvailableNumber(schedule.getAvailableNumber() + 1);
        } else {
            //下单成功更新预约数
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
        }
        scheduleService.update(schedule);
        //订单更新成功后发送短信mq
        MsmVo msmVo = orderMqVo.getMsmVo();
        if (!Objects.isNull(msmVo)) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }
}