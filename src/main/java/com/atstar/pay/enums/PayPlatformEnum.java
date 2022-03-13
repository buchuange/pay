package com.atstar.pay.enums;

import com.lly835.bestpay.enums.BestPayTypeEnum;
import lombok.Getter;

@Getter
public enum PayPlatformEnum {


    WX(1),

    ALIPAY(2);

    private Integer code;

    PayPlatformEnum(Integer code) {
        this.code = code;
    }

    public static PayPlatformEnum getByBestPayTypeEnum(BestPayTypeEnum bestPayTypeEnum) {
        for (PayPlatformEnum payPlatformEnum : PayPlatformEnum.values()) {
            if (bestPayTypeEnum.getPlatform().name().equals(payPlatformEnum.name())) {
                return payPlatformEnum;
            }
        }
        throw new RuntimeException("错误的支付平台：" + bestPayTypeEnum.name());
    }
}
