package com.stt.yygh.user.client;

import com.stt.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-user")
@Component
public interface PatientFeignClient {

    //获取就诊人信息
    @GetMapping("/api/user/patient/inner/get/{id}")
    Patient getPatientInfo(@PathVariable("id") Long id);
}
