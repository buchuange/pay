package com.atstar.pay.service.impl;

import com.atstar.pay.domain.PayInfo;
import com.atstar.pay.enums.PayPlatformEnum;
import com.atstar.pay.mapper.PayInfoMapper;
import com.atstar.pay.service.IPayService;
import com.google.gson.Gson;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @Author: Dawn
 * @Date: 2022/3/1 16:00
 */
@Slf4j
@Service
public class PayServiceImpl implements IPayService {

    @Autowired
    private BestPayService bestPayService;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String QUEUE_PAY_NOTIFY = "payNotify";

    @Transactional
    @Override
    public PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum) {

        // 将支付信息写入数据库
        PayInfo payInfo = new PayInfo();
        payInfo.setOrderNo(orderId);
        payInfo.setPayPlatform(PayPlatformEnum.getByBestPayTypeEnum(bestPayTypeEnum).getCode());
        payInfo.setPlatformStatus(OrderStatusEnum.NOTPAY.name());
        payInfo.setPayAmount(amount);

        payInfoMapper.insertSelective(payInfo);

        PayRequest payRequest = new PayRequest();
        payRequest.setPayTypeEnum(bestPayTypeEnum);
        payRequest.setOrderId(orderId);
        payRequest.setOrderName("练习用支付订单");
        payRequest.setOrderAmount(amount.doubleValue());

        PayResponse payResponse = bestPayService.pay(payRequest);
        log.info("发起支付 response：{}", payResponse);

        return payResponse;
    }

    @Transactional
    @Override
    public String asyncNotify(String notifyData) {
        // 1、签名校验
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("异步通知 payResponse: {}", payResponse);

        // 2、金额校验（从数据库查订单）
        PayInfo payInfo = payInfoMapper.selectByOrderNo(payResponse.getOrderId());
        // 比较严重（正常情况下是不会发生的） 发出告警：钉钉、短信
        if (payInfo == null) {
            throw new RuntimeException("通过orderNo查询到的结果是Null");
        }

        // 如果订单状态不是已支付
        if (!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())) {
            if (payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) != 0) {
                throw new RuntimeException("异步通知的金额和数据库中保存的金额不一致，orderNo=" + payResponse.getOrderId());
            }

            // 修改订单支付状态
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }

        //TODO pay发送MQ消息，mall接收MQ消息
        amqpTemplate.convertAndSend(QUEUE_PAY_NOTIFY, new Gson().toJson(payInfo));

        String result;

        if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            // 告诉微信不要再通知了
            result = "<xml>\n" +
                    "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    "  <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        } else if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
            // 告诉支付宝不要再通知了
            result = "success";
        } else {
            throw new RuntimeException("异步通知中错误的支付平台");
        }

        return result;
    }

    @Override
    public PayInfo queryByOrderId(String orderId) {

        return payInfoMapper.selectByOrderNo(orderId);
    }
}
