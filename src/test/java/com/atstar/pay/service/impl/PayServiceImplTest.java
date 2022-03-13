package com.atstar.pay.service.impl;

import com.atstar.pay.PayApplicationTests;
import com.atstar.pay.service.IPayService;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;


class PayServiceImplTest extends PayApplicationTests {

    @Autowired
    private IPayService payService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    void create() {
        payService.create("1234561321", BigDecimal.valueOf(0.01), BestPayTypeEnum.WXPAY_NATIVE);
    }

    @Test
    void sendMQMsg() {
        amqpTemplate.convertAndSend("payNotify", "Hello RabbitMQ!");
    }
}