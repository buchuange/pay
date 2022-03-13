package com.atstar.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: Dawn
 * @Date: 2022/3/3 22:27
 */
@Component
@Data
@ConfigurationProperties(prefix = "wx")
public class WxAccountConfig {

    private String appId;

    private String mchId;

    private String mchKey;

    private String notifyUrl;

    private String returnUrl;
}
