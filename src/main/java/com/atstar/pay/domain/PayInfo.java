package com.atstar.pay.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PayInfo {
    private Integer id;

    private Integer userId;

    private String orderNo;

    private Integer payPlatform;

    private String platformNumber;

    private String platformStatus;

    private BigDecimal payAmount;

    private Date createTime;

    private Date updateTime;
}