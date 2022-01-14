package com.stt.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.stt.yygh.cmn.client.DictFeignClient;
import com.stt.yygh.enums.DictEnum;
import com.stt.yygh.hosp.repository.HospitalRepository;
import com.stt.yygh.hosp.service.HospitalService;
import com.stt.yygh.model.hosp.Hospital;
import com.stt.yygh.vo.hosp.HospitalQueryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Slf4j
@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository repository;

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public void save(Map<String, Object> paramMap) {
        log.info(JSONObject.toJSONString(paramMap));
        Hospital hospital = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Hospital.class);
        //判断是否存在
        Hospital targetHospital = repository.getHospitalByHoscode(hospital.getHoscode());
        if (ObjectUtils.isEmpty(targetHospital)) {
            //0：未上线 1：已上线
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
        } else {
            hospital.setStatus(targetHospital.getStatus());
            hospital.setCreateTime(targetHospital.getCreateTime());
        }
        hospital.setUpdateTime(new Date());
        hospital.setIsDeleted(0);
        repository.save(hospital);
    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        return repository.getHospitalByHoscode(hoscode);
    }

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        //0为第一页
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> pages = repository.findAll(example, pageable);

        pages.getContent().stream().forEach(h -> this.packHospital(h));
        return pages;
    }

    /**
     * 封装数据
     *
     * @param hospital
     * @return
     */
    private Hospital packHospital(Hospital hospital) {
        // 获取医院等级
        String hosptypeStr = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(), hospital.getHostype());
        // 获取省市区地址
        String provinceStr = dictFeignClient.getName(hospital.getProvinceCode());
        String cityStr = dictFeignClient.getName(hospital.getCityCode());
        String districtStr = dictFeignClient.getName(hospital.getDistrictCode());
        // 将查询出的字段放入到param扩展字段中
        hospital.getParam().put("hostypeString", hosptypeStr);
        hospital.getParam().put("fullAddress", provinceStr + cityStr + districtStr);
        return hospital;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        if (status.intValue() == 0 || status.intValue() == 1) {
            Hospital hospital = repository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            repository.save(hospital);
        }
    }

    @Override
    public Map<String, Object> show(String id) {
        Map<String, Object> result = new HashMap<>();
        Hospital hospital = this.packHospital(repository.findById(id).get());
        result.put("hospital", hospital);
        //单独处理更直观
        result.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        // 返回值最好使用一个VO，这样里面的字段都很清晰，虽然返回Map扩展性很高，但是不好维护
        return result;
    }

    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = repository.getHospitalByHoscode(hoscode);
        if (Objects.isNull(hospital)) {
            return "";
        }
        return hospital.getHosname();
    }

    @Override
    public List<Hospital> findByHosname(String hosname) {
        return repository.findHospitalByHosnameLike(hosname);
    }

    @Override
    public Map<String, Object> detail(String hoscode) {
        Map<String, Object> re = new HashMap<>();
        //医院详情
        Hospital hospital = this.packHospital(this.getByHoscode(hoscode));
        re.put("hospital", hospital);
        //预约规则
        re.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return re;
    }
}
