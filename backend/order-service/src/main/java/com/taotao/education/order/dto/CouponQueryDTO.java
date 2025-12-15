package com.taotao.education.order.dto;

import lombok.Data;

@Data
public class CouponQueryDTO {
    private Integer status; // 0未使用 1已使用 2过期
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}


