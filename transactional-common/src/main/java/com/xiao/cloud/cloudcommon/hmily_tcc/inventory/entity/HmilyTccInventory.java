package com.xiao.cloud.cloudcommon.hmily_tcc.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author aloneMan
 * @projectName distributed-transaction
 * @createTime 2022-11-18 09:40:01
 * @description
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HmilyTccInventory {
    private Long id;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 总库存
     */
    private Integer totalInventory;

    /**
     * 锁定库存
     */
    private Integer lockInventory;
}