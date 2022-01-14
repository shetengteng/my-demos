package com.stt.yygh.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stt.yygh.common.exception.YyghException;
import com.stt.yygh.common.result.ResultCodeEnum;
import com.stt.yygh.hosp.mapper.HospitalSetMapper;
import com.stt.yygh.hosp.service.HospitalSetService;
import com.stt.yygh.model.hosp.HospitalSet;
import com.stt.yygh.vo.hosp.SignInfoVo;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {
    // 由于传递了 HospitalSetMapper ，在 ServiceImpl 中进行了 Autowired操作
    // 因此可以直接通过 baseMapper 变量调用  HospitalSetMapper 中的方法
    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sign_key").eq("hoscode", hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(queryWrapper);
        return hospitalSet.getSignKey();
    }

    //获取医院签名信息
    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if(Objects.isNull(hospitalSet)) {
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;
    }

}
