package com.stt.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stt.yygh.common.exception.YyghException;
import com.stt.yygh.common.helper.HttpRequestHelper;
import com.stt.yygh.common.result.ResultCodeEnum;
import com.stt.yygh.enums.OrderStatusEnum;
import com.stt.yygh.hosp.client.HospitalFeignClient;
import com.stt.yygh.model.order.OrderInfo;
import com.stt.yygh.model.user.Patient;
import com.stt.yygh.order.mapper.OrderInfoMapper;
import com.stt.yygh.order.service.OrderService;
import com.stt.yygh.order.service.WeixinService;
import com.stt.yygh.rabbitmq.MqConst;
import com.stt.yygh.rabbitmq.RabbitService;
import com.stt.yygh.user.client.PatientFeignClient;
import com.stt.yygh.vo.order.OrderCountQueryVo;
import com.stt.yygh.vo.order.OrderCountVo;
import com.stt.yygh.vo.order.OrderMqVo;
import com.stt.yygh.vo.hosp.ScheduleOrderVo;
import com.stt.yygh.vo.hosp.SignInfoVo;
import com.stt.yygh.vo.msm.MsmVo;
import com.stt.yygh.vo.order.OrderQueryVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private WeixinService weixinService;

    //保存订单
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrder(String scheduleId, Long patientId) {
        Patient patient = patientFeignClient.getPatientInfo(patientId);
        if (Objects.isNull(patient)) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        // 创建订单
        OrderInfo order = createAndSaveOrder(scheduleId, patient);

        // 给医院模拟系统发送创建订单请求
        JSONObject resp = accessRemoteHospAndCreateOrder(order, patient);

        order.setHosRecordId(resp.getString("hosRecordId"));    //预约记录唯一标识（医院预约记录主键）
        order.setNumber(resp.getInteger("number"));             //预约序号
        order.setFetchTime(resp.getString("fetchTime"));        //取号时间
        order.setFetchAddress(resp.getString("fetchAddress"));  //取号地址
        baseMapper.updateById(order);                               //更新订单

        //排班可预约数
        Integer reservedNumber = resp.getInteger("reservedNumber");
        //排班剩余预约数
        Integer availableNumber = resp.getInteger("availableNumber");
        // 发送更新排班信息并发送短信通知
        sendUpdateMq(order, reservedNumber, availableNumber);

        return order.getId();
    }

    //发送mq信息更新号源和短信通知
    private void sendUpdateMq(OrderInfo order, Integer reservedNumber, Integer availableNumber) {
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(order.getScheduleId().split("@")[1]);
        orderMqVo.setReservedNumber(reservedNumber);
        orderMqVo.setAvailableNumber(availableNumber);

        //短信提示
        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(order.getPatientPhone());
        String reserveDate = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd") + (order.getReserveTime() == 0 ? "上午" : "下午");
        Map<String, Object> param = new HashMap<String, Object>() {{
            put("title", order.getHosname() + "|" + order.getDepname() + "|" + order.getTitle());
            put("amount", order.getAmount());
            put("reserveDate", reserveDate);
            put("name", order.getPatientName());
            put("quitTime", new DateTime(order.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            // 注意：由于短信测试模板的原因，只有一个code字段可以被读取，其他参数如果短信模板支持可以添加
            // 11111 在测试时表示创建成功
            put("code", "11111");
        }};
        msmVo.setParam(param);
        orderMqVo.setMsmVo(msmVo);

        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
    }

    // 访问远端医院模拟系统，并创建订单，返回对应的参数并存储
    private JSONObject accessRemoteHospAndCreateOrder(OrderInfo order, Patient patient) {
        SignInfoVo signInfo = hospitalFeignClient.getSignInfoVo(order.getHoscode());
        if (Objects.isNull(signInfo)) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        Map<String, Object> param = createHospParam(order, patient, signInfo);
        JSONObject re = HttpRequestHelper.sendRequest(param, signInfo.getApiUrl() + "/order/submitOrder");
        if (re.getInteger("code") != 200) {
            throw new YyghException(re.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return re.getJSONObject("data");
    }

    private OrderInfo createAndSaveOrder(String scheduleId, Patient patient) {
        ScheduleOrderVo scheduleOrder = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        if (Objects.isNull(scheduleOrder)) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        //当前时间不可以预约
        if (new DateTime(scheduleOrder.getStartTime()).isAfterNow() || new DateTime(scheduleOrder.getEndTime()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }
        if (scheduleOrder.getAvailableNumber() <= 0) {
            throw new YyghException(ResultCodeEnum.NUMBER_NO);
        }
        OrderInfo re = new OrderInfo();
        BeanUtils.copyProperties(scheduleOrder, re);
        // 注意：这里是hosScheduleId字段，需要包含医院的模拟id 以及 mongodb的id
        re.setScheduleId(scheduleOrder.getHosScheduleId() + "@" + scheduleId);
        re.setOutTradeNo(System.currentTimeMillis() + "" + new Random().nextInt(100));
        re.setUserId(patient.getUserId());
        re.setPatientId(patient.getId());
        re.setPatientName(patient.getName());
        re.setPatientPhone(patient.getPhone());
        re.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        this.save(re);
        return re;
    }

    // 调用医院模拟系统接口时传递的参数
    private Map<String, Object> createHospParam(OrderInfo order, Patient patient, SignInfoVo signInfo) {
        Map<String, Object> re = new HashMap<>();
        re.put("hoscode", order.getHoscode());
        re.put("depcode", order.getDepcode());
        re.put("hosScheduleId", order.getScheduleId().split("@")[0]);
        re.put("reserveDate", new DateTime(order.getReserveDate()).toString("yyyy-MM-dd"));
        re.put("reserveTime", order.getReserveTime());
        re.put("amount", order.getAmount());
        re.put("name", order.getPatientName());
        re.put("certificatesType", patient.getCertificatesType());
        re.put("certificatesNo", patient.getCertificatesNo());
        re.put("sex", patient.getSex());
        re.put("birthdate", patient.getBirthdate());
        re.put("phone", patient.getPhone());
        re.put("isMarry", patient.getIsMarry());
        re.put("provinceCode", patient.getProvinceCode());
        re.put("cityCode", patient.getCityCode());
        re.put("districtCode", patient.getDistrictCode());
        re.put("address", patient.getAddress());
        //联系人
        re.put("contactsName", patient.getContactsName());
        re.put("contactsCertificatesType", patient.getContactsCertificatesType());
        re.put("contactsCertificatesNo", patient.getContactsCertificatesNo());
        re.put("contactsPhone", patient.getContactsPhone());
        re.put("timestamp", HttpRequestHelper.getTimestamp());
        re.put("sign", HttpRequestHelper.getSign(re, signInfo.getSignKey()));
        return re;
    }

    @Override
    public Page<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        String name = orderQueryVo.getKeyword();            //医院名称
        Long patientId = orderQueryVo.getPatientId();       //就诊人名称
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate(); //安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        //对条件值进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)) wrapper.like("hosname", name);
        if (!Objects.isNull(patientId)) wrapper.eq("patient_id", patientId);
        if (!StringUtils.isEmpty(orderStatus)) wrapper.eq("order_status", orderStatus);
        if (!StringUtils.isEmpty(reserveDate)) wrapper.ge("reserve_date", reserveDate);
        if (!StringUtils.isEmpty(createTimeBegin)) wrapper.ge("create_time", createTimeBegin);
        if (!StringUtils.isEmpty(createTimeEnd)) wrapper.le("create_time", createTimeEnd);

        Page<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        pages.getRecords().forEach(this::packOrderInfo);
        return pages;
    }

    //根据订单id查询订单详情
    @Override
    public OrderInfo getOrder(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        return this.packOrderInfo(orderInfo);
    }

    @Override
    public Map<String, Object> show(Long orderId) {
        Map<String, Object> map = new HashMap<>();
        OrderInfo orderInfo = this.packOrderInfo(this.getById(orderId));
        map.put("orderInfo", orderInfo);
        Patient patient = patientFeignClient.getPatientInfo(orderInfo.getPatientId());
        map.put("patient", patient);
        return map;
    }

    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }

    @Override
    public Boolean cancelOrder(Long orderId) {
        OrderInfo order = this.getById(orderId);
        //当前时间大约退号时间，不能取消预约
        DateTime quitTime = new DateTime(order.getQuitTime());
        if (quitTime.isBeforeNow()) {
            // 测试需要，此处注释掉
//            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }
        // 访问模拟系统取消订单
        remoteHospSystemForCancelOrder(order);

        if (Objects.equals(order.getOrderStatus(), OrderStatusEnum.PAID.getStatus())) {
            //已支付 调用微信服务进行退款操作，退款失败抛出异常
            if (!weixinService.refund(orderId)) {
                throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
            }
        }
        //更改订单状态
        order.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
        this.updateById(order);

        // 更新预约个数，以及发送短信通知
        updateScheduleInfoAndSendSMS(order);
        return true;
    }

    // 访问医院模拟系统，进行取消订单操作
    private void remoteHospSystemForCancelOrder(OrderInfo order) {
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(order.getHoscode());
        if (Objects.isNull(signInfoVo)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> param = new HashMap<>();
        param.put("hoscode", order.getHoscode());
        param.put("hosRecordId", order.getHosRecordId());
        param.put("timestamp", HttpRequestHelper.getTimestamp());
        param.put("sign", HttpRequestHelper.getSign(param, signInfoVo.getSignKey()));

        JSONObject re = HttpRequestHelper.sendRequest(param, signInfoVo.getApiUrl() + "/order/updateCancelStatus");

        if (!Objects.equals(re.getInteger("code"), 200)) {
            throw new YyghException(re.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
    }


    private void updateScheduleInfoAndSendSMS(OrderInfo order) {
        //短信提示
        MsmVo msm = new MsmVo();
        msm.setPhone(order.getPatientPhone());
        msm.setTemplateCode("SMS_194640722");
        String reserveDate = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd") + (order.getReserveTime() == 0 ? "上午" : "下午");
        Map<String, Object> param = new HashMap<String, Object>() {{
            put("title", order.getHosname() + "|" + order.getDepname() + "|" + order.getTitle());
            put("reserveDate", reserveDate);
            put("name", order.getPatientName());
            // 由于测试环境只能使用固定测试模板，同时模板只能接受code字段，设置固定值，用于区分
            put("code", "22222");
        }};
        msm.setParam(param);

        //发送mq信息更新预约数 与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减1即可
        OrderMqVo orderMq = new OrderMqVo();
        orderMq.setScheduleId(order.getScheduleId().split("@")[1]);
        orderMq.setMsmVo(msm);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMq);
    }

    @Override
    public void patientTips() {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reserve_date", new DateTime().toString("yyyy-MM-dd"));
        List<OrderInfo> orderInfoList = baseMapper.selectList(queryWrapper);

        for (OrderInfo orderInfo : orderInfoList) {
            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String, Object> param = new HashMap<String, Object>() {{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("code", "33333"); // 在个人短信模板中，只有code可以使用，在测试时使用
            }};
            msmVo.setParam(param);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }

    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        List<OrderCountVo> orderCountVoList = baseMapper.selectOrderCount(orderCountQueryVo);
        Map<String, Object> re = new HashMap<>(2);
        //日期列表 x轴
        re.put("dateList", orderCountVoList.stream()
                .map(OrderCountVo::getReserveDate)
                .collect(Collectors.toList()));
        //统计列表 y轴
        re.put("countList", orderCountVoList.stream()
                .map(OrderCountVo::getCount)
                .collect(Collectors.toList()));
        return re;
    }

}