package com.stt.yygh.msm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.stt.yygh.msm.config.ConstantPropertiesUtils;
import com.stt.yygh.msm.service.MsmService;
import com.stt.yygh.vo.msm.MsmVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class MsmServiceImpl implements MsmService {

    // 初始化账号客户端
    public static Client createClient() throws Exception {
        Config config = new Config()
                .setEndpoint("dysmsapi.aliyuncs.com")   // 访问的域名
                .setAccessKeyId(ConstantPropertiesUtils.ACCESS_KEY_ID)            // AccessKey ID
                .setAccessKeySecret(ConstantPropertiesUtils.SECRECT);   // AccessKey Secret
        return new Client(config);
    }

    //整合阿里云短信服务
    @Override
    public boolean send(String phone, String code) {
        //判断手机号是否为空
        if (StringUtils.isEmpty(phone)) {
            return false;
        }
        //验证码  使用json格式   {"code":"123456"}
        Map<String, Object> param = new HashMap();
        param.put("code", code);
        try {
            Client client = createClient();
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setSignName("阿里云短信测试")             //签名名称
                    .setTemplateCode("SMS_154950909")       //模板code
                    .setPhoneNumbers(phone)                 //手机号
                    .setTemplateParam(JSONObject.toJSONString(param)); // 验证码
            //调用方法进行短信发送
            SendSmsResponse response = client.sendSms(sendSmsRequest);
            System.out.println(response.getBody());
            if (Objects.equals(response.getBody().getCode(), "OK")) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 由于使用的是同一个短信测试模板，实际开发中需要申请不同的短信模板
    @Override
    public boolean send(MsmVo msmVo) {
        if(!StringUtils.isEmpty(msmVo.getPhone())) {
            String code = (String)msmVo.getParam().get("code");
            return this.send(msmVo.getPhone(),code);
        }
        return false;
    }
}
