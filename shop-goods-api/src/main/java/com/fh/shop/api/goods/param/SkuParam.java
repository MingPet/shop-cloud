package com.fh.shop.api.goods.param;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SkuParam implements Serializable {

    private String skuName;
    private BigDecimal price;
    private String brandName;
    private String cateName;
    private Integer stock;

}
