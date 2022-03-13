package com.atstar.pay.controller;

import com.atstar.pay.domain.PayInfo;
import com.atstar.pay.service.IPayService;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;

/**
 * @Author: Dawn
 * @Date: 2022/3/1 21:13
 */
@Slf4j
@Controller
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private IPayService payService;

    @Autowired
    private WxPayConfig wxPayConfig;

    @GetMapping("/create")
    public ModelAndView create(@RequestParam("orderId") String orderId,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam("payType") BestPayTypeEnum bestPayTypeEnum) {

        PayResponse payResponse = payService.create(orderId, amount, bestPayTypeEnum);

        ModelAndView view = new ModelAndView();

        // 支付方式不同，渲染方式不同，WXPAY_NATIVE使用codeUrl, ALIPAY_PC使用body
        if (bestPayTypeEnum == BestPayTypeEnum.WXPAY_NATIVE) {
            view.addObject("codeUrl", payResponse.getCodeUrl());
            view.addObject("orderId", orderId);
            view.addObject("returnUrl", wxPayConfig.getReturnUrl());
            view.setViewName("createForWxNative");
        } else if (bestPayTypeEnum == BestPayTypeEnum.ALIPAY_PC) {
            view.addObject("body", payResponse.getBody());
            view.setViewName("createForAlipayPc");
        }

        return view;
    }

    @PostMapping("/notify")
    @ResponseBody
    public String asyncNotify(@RequestBody String notifyData) {
        return payService.asyncNotify(notifyData);
    }

    @GetMapping("/queryByOrderId")
    @ResponseBody
    public PayInfo queryByOrderId(@RequestParam("orderId") String orderId) {
        return payService.queryByOrderId(orderId);
    }

}
