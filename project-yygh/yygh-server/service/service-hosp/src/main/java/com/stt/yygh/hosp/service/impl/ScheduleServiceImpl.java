package com.stt.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.stt.yygh.common.exception.YyghException;
import com.stt.yygh.common.result.ResultCodeEnum;
import com.stt.yygh.hosp.repository.ScheduleRepository;
import com.stt.yygh.hosp.service.DepartmentService;
import com.stt.yygh.hosp.service.HospitalService;
import com.stt.yygh.hosp.service.ScheduleService;
import com.stt.yygh.model.hosp.BookingRule;
import com.stt.yygh.model.hosp.Department;
import com.stt.yygh.model.hosp.Hospital;
import com.stt.yygh.model.hosp.Schedule;
import com.stt.yygh.vo.hosp.BookingScheduleRuleVo;
import com.stt.yygh.vo.hosp.ScheduleOrderVo;
import com.stt.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public void save(Map<String, Object> paramMap) {
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Schedule.class);
        Schedule targetSchedule = repository.getByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        // 表示不存在该排班，则添加
        if (Objects.isNull(targetSchedule)) {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setStatus(1);
            schedule.setIsDeleted(0);
        } else {
            schedule.setUpdateTime(new Date());
            schedule.setStatus(1);
            schedule.setIsDeleted(0);
            // 有id则更新
            schedule.setId(targetSchedule.getId());
        }
        repository.save(schedule);
    }

    @Override
    public Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo) {

        // 分页参数
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        // 创建匹配器，条件查询
        ExampleMatcher exampleMatcher = ExampleMatcher
                .matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        // 查询参数
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);
        schedule.setStatus(1);
        Example<Schedule> example = Example.of(schedule, exampleMatcher);

        Page<Schedule> re = repository.findAll(example, pageable);
        return re;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = repository.getByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (Objects.isNull(schedule)) {
            return;
        }
        repository.deleteById(schedule.getId());
    }

    //根据医院编号 和 科室编号 ，查询排班规则数据
    @Override
    public Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode) {
        // 依据医院编号，科室编号查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode);
        // 依据工作日进行分组
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria), // 匹配条件
                Aggregation.group("workDate") // 分组字段
                        .first("workDate").as("workDate") // 根据资源文档的排序获取第一个文档数据
                        .count().as("docCount") // 统计号源个数
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.DESC, "workDate"),// 排序
                Aggregation.skip((page - 1) * limit), // 分页
                Aggregation.limit(limit)
        );
        AggregationResults<BookingScheduleRuleVo> aggResults = mongoTemplate.aggregate(agg,
                Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResults.getMappedResults();

        // 分页查询的总记录数
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResults = mongoTemplate.aggregate(totalAgg,
                Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggResults.getMappedResults().size();

        // 将日期对应的星期获取
        for (BookingScheduleRuleVo vo : bookingScheduleRuleVoList) {
            Date workDate = vo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            vo.setDayOfWeek(dayOfWeek);
        }

        //设置最终数据，进行返回
        Map<String, Object> re = new HashMap<>();
        re.put("bookingScheduleRuleList", bookingScheduleRuleVoList);
        re.put("total", total);
        //获取医院名称
        String hosName = hospitalService.getHospName(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hosName);
        re.put("baseMap", baseMap);

        return re;
    }

    private String getDayOfWeek(DateTime dateTime) {
        Map<Integer, String> dayOfWeekMap = new HashMap<>();
        dayOfWeekMap.put(DateTimeConstants.MONDAY, "周一");
        dayOfWeekMap.put(DateTimeConstants.TUESDAY, "周二");
        dayOfWeekMap.put(DateTimeConstants.WEDNESDAY, "周三");
        dayOfWeekMap.put(DateTimeConstants.THURSDAY, "周四");
        dayOfWeekMap.put(DateTimeConstants.FRIDAY, "周五");
        dayOfWeekMap.put(DateTimeConstants.SATURDAY, "周六");
        dayOfWeekMap.put(DateTimeConstants.SUNDAY, "周日");
        if (dayOfWeekMap.containsKey(dateTime.getDayOfWeek())) {
            return dayOfWeekMap.get(dateTime.getDayOfWeek());
        }
        return "";
    }

    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        List<Schedule> scheduleList = repository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode,
                new DateTime(workDate).toDate());
        scheduleList.forEach(this::packageSchedule);
        return scheduleList;
    }

    // 设置其他值：医院名称、科室名称、日期对应星期
    private Schedule packageSchedule(Schedule s) {
        //设置医院名称
        s.getParam().put("hosname", hospitalService.getHospName(s.getHoscode()));
        //设置科室名称
        s.getParam().put("depname", departmentService.getDepName(s.getHoscode(), s.getDepcode()));
        return s;
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(int page, int limit, String hoscode, String depcode) {

        //获取预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (Objects.isNull(hospital)) throw new YyghException(ResultCodeEnum.DATA_ERROR);
        BookingRule bookingRule = hospital.getBookingRule();

        //获取可预约日期分页数据
        IPage<Date> datePage = this.getListDate(page, limit, bookingRule);

        //获取可预约日期科室剩余预约数
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria
                        .where("hoscode").is(hoscode)
                        .and("depcode").is(depcode)
                        .and("workDate").in(datePage.getRecords())), //当前页可预约日期
                Aggregation.group("workDate")//分组字段
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        //获取科室剩余预约数
        List<BookingScheduleRuleVo> scheduleVoList = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class)
                .getMappedResults();
        //合并数据 将统计数据ScheduleVo根据“安排日期”合并到BookingRuleVo
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = scheduleVoList.stream()
                .collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, v -> v));

        //获取可预约排班规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0, len = datePage.getRecords().size(); i < len; i++) {
            Date date = datePage.getRecords().get(i);
            BookingScheduleRuleVo rule = scheduleVoMap.get(date);
            if (Objects.isNull(rule)) { // 说明当天没有排班医生
                rule = new BookingScheduleRuleVo();
                rule.setDocCount(0);   //就诊医生人数
                rule.setAvailableNumber(-1);  //科室剩余预约数  -1表示无号
            }
            rule.setWorkDate(date);
            rule.setWorkDateMd(date);
            rule.setDayOfWeek(this.getDayOfWeek(new DateTime(date))); //计算当前预约日期为周几

            //最后一页最后一条记录为即将预约  状态 0：正常 1：即将放号 -1：当天已停止挂号
            if (i == len - 1 && page == datePage.getPages()) {
                rule.setStatus(1);
            } else {
                rule.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if (i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopTime.isBeforeNow()) rule.setStatus(-1);  //停止预约
            }
            bookingScheduleRuleVoList.add(rule);
        }

        Map<String, Object> re = new HashMap<>();
        //可预约日期规则数据
        re.put("bookingScheduleList", bookingScheduleRuleVoList);
        re.put("total", datePage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department = departmentService.getDepartment(hoscode, depcode);
        baseMap.put("bigname", department.getBigname());         //大科室名称
        baseMap.put("depname", department.getDepname());         //科室名称
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月")); //月
        baseMap.put("releaseTime", bookingRule.getReleaseTime()); //放号时间
        baseMap.put("stopTime", bookingRule.getStopTime()); //停号时间
        re.put("baseMap", baseMap);
        return re;
    }

    @Override
    public Schedule getById(String id) {
        Schedule schedule = repository.findById(id).get();
        return this.packageSchedule(schedule);
    }

    //根据排班id获取预约下单数据
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        //排班信息
        Schedule schedule = this.getById(scheduleId);
        if (Objects.isNull(schedule)) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        //获取预约规则信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if (Objects.isNull(hospital) || Objects.isNull(hospital.getBookingRule())) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        return parseScheduleOrderVo(schedule, hospital.getBookingRule());
    }

    private ScheduleOrderVo parseScheduleOrderVo(Schedule schedule, BookingRule rule) {
        ScheduleOrderVo re = new ScheduleOrderVo();
        re.setHoscode(schedule.getHoscode());
        re.setDepcode(schedule.getDepcode());
        re.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        re.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        re.setHosScheduleId(schedule.getHosScheduleId());
        re.setAvailableNumber(schedule.getAvailableNumber());
        re.setTitle(schedule.getTitle());
        re.setReserveDate(schedule.getWorkDate());
        re.setReserveTime(schedule.getWorkTime());
        re.setAmount(schedule.getAmount());
        //退号截止天数（如：就诊前一天为-1，当天为0）
        re.setQuitTime(this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(rule.getQuitDay()).toDate(), rule.getQuitTime()).toDate());
        //预约开始时间
        re.setStartTime(this.getDateTime(new Date(), rule.getReleaseTime()).toDate());
        //预约截止时间
        re.setEndTime(this.getDateTime(new DateTime().plusDays(rule.getCycle()).toDate(), rule.getStopTime()).toDate());
        //当天停止挂号时间
        re.setStopTime(this.getDateTime(new Date(), rule.getStopTime()).toDate());
        return re;
    }


    /**
     * 获取可预约日期分页数据
     */
    private IPage<Date> getListDate(int page, int limit, BookingRule bookingRule) {
        // 由于测试数据的预约挂号时间是2021-03-02开始，因此可以设置固定时间用于前端数据显示，供测试使用，后期测试完成后需要删除
        DateTime testDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime("2021-01-01");
        DateTime releaseTime = this.getDateTime(testDate.toDate(), bookingRule.getReleaseTime());

        //当天放号时间
//        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //预约周期
        int cycle = bookingRule.getCycle();
        //如果当天放号时间已过，则预约周期后一天为即将放号时间，周期加1
        if (releaseTime.isBeforeNow()) cycle += 1;
        //可预约所有日期，最后一天显示即将放号倒计时
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            // 使用如下语句只是用于测试使用，后期需要删除
            DateTime curDateTime = testDate.plusDays(i);

            //计算当前预约日期
//            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        //日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
        List<Date> pageDateList = new ArrayList<>();
        int start = (page - 1) * limit;
        int end = (page - 1) * limit + limit;
        if (end > dateList.size()) end = dateList.size();
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }
        IPage<Date> re = new com.baomidou.mybatisplus.extension.plugins.pagination.Page(page, 7, dateList.size());
        re.setRecords(pageDateList);
        return re;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
    }

    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        //主键一致就是更新
        repository.save(schedule);
    }
}
