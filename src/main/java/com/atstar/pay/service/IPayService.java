package com.atstar.pay.service;

import com.atstar.pay.domain.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

public interface IPayService {


    /**
     * 创建/发起支付
     * @param orderId 订单号
     * @param amount  支付金额
     */
    PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

    /**
     * 异步通知处理
     * @param notifyData 通知数据
     */
    String asyncNotify(String notifyData);

    /**
     * 根据订单号查询支付状态
     * @param orderId
     * @return
     */
    PayInfo queryByOrderId(String orderId);
}
