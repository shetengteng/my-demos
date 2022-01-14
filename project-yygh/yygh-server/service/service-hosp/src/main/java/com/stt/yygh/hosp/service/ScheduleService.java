package com.stt.yygh.hosp.service;

import com.stt.yygh.model.hosp.Schedule;
import com.stt.yygh.vo.hosp.ScheduleOrderVo;
import com.stt.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    void save(Map<String, Object> paramMap);

    Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode);

    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

    // 获取排班可预约日期数据
    Map<String, Object> getBookingScheduleRule(int page, int limit, String hoscode, String depcode);

    // 根据id获取排班
    Schedule getById(String id);

    //根据排班id获取预约下单数据
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    // 修改排班
    void update(Schedule schedule);
}
