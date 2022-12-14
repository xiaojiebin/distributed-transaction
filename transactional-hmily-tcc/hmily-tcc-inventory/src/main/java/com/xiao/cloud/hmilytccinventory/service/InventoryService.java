package com.xiao.cloud.hmilytccinventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiao.cloud.cloudcommon.hmily_tcc.inventory.dto.InventoryDTO;
import com.xiao.cloud.cloudcommon.hmily_tcc.inventory.entity.HmilyTccInventory;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author aloneMan
 * @projectName distributed-transaction
 * @createTime 2022-11-18 09:40:41
 * @description
 */
public interface InventoryService extends IService<HmilyTccInventory> {

    /**
     * 扣除库存
     *
     * @param inventoryDTO
     *         扣除信息
     * @return 返回扣减后的数量
     */
    HmilyTccInventory decreaseInventory(InventoryDTO inventoryDTO);

    /**
     * 扣除库存-异常
     *
     * @param inventoryDTO
     *         扣除信息
     * @return 返回扣减后的数量
     */
    HmilyTccInventory decreaseInventoryException(InventoryDTO inventoryDTO);

    /**
     * 扣除库存-超时
     *
     * @param inventoryDTO
     *         扣除信息
     * @return 返回扣减后的数量
     */
    HmilyTccInventory decreaseInventoryTimeout(InventoryDTO inventoryDTO);

    /**
     * 扣除库存提交
     *
     * @param inventoryDTO
     *         扣除信息
     * @return 返回扣减后的数量
     */
    /*MessageTxInventory commit(InventoryDTO inventoryDTO);

    *//**
     * 扣除库存回滚
     *
     * @param inventoryDTO
     *         扣除数量
     * @return 返回扣减后的数量
     */
    /*MessageTxInventory rollback(InventoryDTO inventoryDTO);*/
}
