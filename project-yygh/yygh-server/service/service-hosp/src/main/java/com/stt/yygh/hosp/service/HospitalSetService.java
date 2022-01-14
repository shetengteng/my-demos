package com.stt.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stt.yygh.model.hosp.HospitalSet;
import com.stt.yygh.vo.hosp.SignInfoVo;

public interface HospitalSetService extends IService<HospitalSet> {
    String getSignKey(String hoscode);

    SignInfoVo getSignInfoVo(String hoscode);
}
