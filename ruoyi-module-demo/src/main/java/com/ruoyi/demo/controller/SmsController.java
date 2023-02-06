package com.ruoyi.demo.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.sms.config.properties.SmsProperties;
import com.ruoyi.sms.core.SmsTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信演示案例
 * !!!不推荐模块内写控制器
 * 请先阅读文档 否则无法使用
 *
 * @author Lion Li
 */
@Validated
@Tag(description = "短信演示案例", name = "SmsDemoService")
@RequiredArgsConstructor
@RestController
@RequestMapping("/demo/sms")
public class SmsController {

    private final SmsProperties smsProperties;
//    private final SmsTemplate smsTemplate; // 可以使用spring注入
//    private final AliyunSmsTemplate smsTemplate; // 也可以注入某个厂家的模板工具

    @Operation(description = "发送短信Aliyun", operationId = "SmsDemoServiceGetSendAliyun")
    @GetMapping("/sendAliyun")
    public R<Object> sendAliyun(@Parameter(description = "电话号") String phones,
                                     @Parameter(description = "模板ID") String templateId) {
        if (!smsProperties.getEnabled()) {
            return R.fail("当前系统没有开启短信功能！");
        }
        if (!SpringUtils.containsBean("aliyunSmsTemplate")) {
            return R.fail("阿里云依赖未引入！");
        }
        SmsTemplate smsTemplate = SpringUtils.getBean(SmsTemplate.class);
        Map<String, String> map = new HashMap<>(1);
        map.put("code", "1234");
        Object send = smsTemplate.send(phones, templateId, map);
        return R.ok(send);
    }

    @Operation(description = "发送短信Tencent", operationId = "SmsDemoServiceGetSendTencent")
    @GetMapping("/sendTencent")
    public R<Object> sendTencent(@Parameter(description = "电话号") String phones,
                                             @Parameter(description = "模板ID") String templateId) {
        if (!smsProperties.getEnabled()) {
            return R.fail("当前系统没有开启短信功能！");
        }
        if (!SpringUtils.containsBean("tencentSmsTemplate")) {
            return R.fail("腾讯云依赖未引入！");
        }
        SmsTemplate smsTemplate = SpringUtils.getBean(SmsTemplate.class);
        Map<String, String> map = new HashMap<>(1);
//        map.put("2", "测试测试");
        map.put("1", "1234");
        Object send = smsTemplate.send(phones, templateId, map);
        return R.ok(send);
    }

}
