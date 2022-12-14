package com.xiao.cloud.hmilytccaccount.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiao.cloud.cloudcommon.hmily_tcc.account.dto.AccountDTO;
import com.xiao.cloud.cloudcommon.hmily_tcc.account.dto.AccountNestedDTO;
import com.xiao.cloud.cloudcommon.hmily_tcc.account.entity.HmilyTccAccount;
import com.xiao.cloud.cloudcommon.hmily_tcc.account.mapper.AccountMapper;
import com.xiao.cloud.cloudcommon.hmily_tcc.inventory.dto.InventoryDTO;
import com.xiao.cloud.hmilytccaccount.openapi.InventoryApi;
import com.xiao.cloud.hmilytccaccount.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.common.utils.GsonUtils;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * @author aloneMan
 * @projectName distributed-transaction
 * @createTime 2022-11-17 15:51:05
 * @description
 */

@Slf4j
@Service("accountService")
public class AccountServiceImpl extends ServiceImpl<AccountMapper, HmilyTccAccount> implements AccountService {

    private final AccountMapper accountMapper;

    private final InventoryApi inventoryApi;

    public AccountServiceImpl(AccountMapper accountMapper, InventoryApi inventoryApi) {
        this.accountMapper = accountMapper;
        this.inventoryApi = inventoryApi;
    }

    @Override
    @HmilyTCC(confirmMethod = "commit", cancelMethod = "rollback")
    public HmilyTccAccount payment(AccountDTO accountDTO) {
        log.info(">>>>>>>>>>> {} 全局事务ID {} <<<<<<<<<<<< ", "执行支付接口try方法", HmilyContextHolder.get().getTransId());
        //扣减金额
        HmilyTccAccount hmilyTccAccount = decrease(accountDTO);
        return hmilyTccAccount;
    }


    @Override
    @HmilyTCC(confirmMethod = "commit", cancelMethod = "rollback")
    public HmilyTccAccount paymentWithTryException(AccountDTO accountDTO) {
        log.info(">>>>>>>>>>> {} 全局事务ID {} <<<<<<<<<<<< ", "执行支付接口paymentWithTryException方法", HmilyContextHolder.get().getTransId());
        throw new RuntimeException(accountDTO.getUserId() + ">> 用户,扣减" + accountDTO.getAmount().doubleValue() + "失败");
    }

    @Override
    @HmilyTCC(confirmMethod = "commit", cancelMethod = "rollback")
    public HmilyTccAccount paymentWithTryTimeOut(AccountDTO accountDTO) {
        log.info(">>>>>>>>>>> {} 全局事务ID {} <<<<<<<<<<<< ", "执行支付接口paymentWithTryTimeOut方法", HmilyContextHolder.get().getTransId());
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //扣减金额
        HmilyTccAccount hmilyTccAccount = decrease(accountDTO);
        return hmilyTccAccount;
    }

    @Override
    @HmilyTCC(confirmMethod = "commitNested", cancelMethod = "rollbackNested")
    public HmilyTccAccount paymentWithNested(AccountNestedDTO accountNestedDTO) {
        log.info(">>>>>>>>>>> {} 全局事务ID {} <<<<<<<<<<<< ", "执行支付接口paymentWithNested方法", HmilyContextHolder.get().getTransId());
        HmilyTccAccount hmilyTccAccount = decrease(buildAccountDTO(accountNestedDTO));
        inventoryApi.decrease(buildInventoryDTO(accountNestedDTO));
        return hmilyTccAccount;
    }

    @Override
    @HmilyTCC(confirmMethod = "commitNested", cancelMethod = "rollbackNested")
    @Transactional
    public HmilyTccAccount paymentWithNestedException(AccountNestedDTO accountNestedDTO) {
        log.info(">>>>>>>>>>> {} 全局事务ID {} <<<<<<<<<<<< ", "执行支付接口paymentWithNestedException方法", HmilyContextHolder.get().getTransId());
        HmilyTccAccount hmilyTccAccount = decrease(buildAccountDTO(accountNestedDTO));
        inventoryApi.decreaseException(buildInventoryDTO(accountNestedDTO));
        return hmilyTccAccount;
    }


    public HmilyTccAccount commit(AccountDTO accountDTO) {
        int commit = accountMapper.commit(accountDTO);
        if (commit >= 0) {
            log.info("用户余额扣减成功 >>> {} ", commit);
        } else {
            log.info("用户余额扣减失败,即将重试");
        }
        HmilyTccAccount hmilyTccAccount = getHmilyTccAccount(accountDTO);
        return hmilyTccAccount;
    }

    public HmilyTccAccount rollback(AccountDTO accountDTO) {
        int rollback = accountMapper.rollback(accountDTO);
        if (rollback >= 0) {
            log.info("用户冻结余额恢复成功 >>> {} ", rollback);
        } else {
            log.info("用户冻结余额恢复失败,即将重试");
        }
        HmilyTccAccount hmilyTccAccount = getHmilyTccAccount(accountDTO);
        return hmilyTccAccount;
    }



    @Transactional(rollbackFor = Exception.class)
    public HmilyTccAccount commitNested(AccountNestedDTO accountNestedDTO) {
        AccountDTO accountDTO = buildAccountDTO(accountNestedDTO);
        int commit = accountMapper.commit(accountDTO);
        if (commit >= 0) {
            log.info("用户余额扣减成功 >>> {} ", commit);
        } else {
            log.info("用户余额扣减失败,即将重试");
        }
        HmilyTccAccount hmilyTccAccount = getHmilyTccAccount(accountDTO);
        return hmilyTccAccount;
    }

    @Transactional(rollbackFor = Exception.class)
    public HmilyTccAccount rollbackNested(AccountNestedDTO accountNestedDTO) {
        AccountDTO accountDTO = buildAccountDTO(accountNestedDTO);
        int rollback = accountMapper.rollback(accountDTO);
        if (rollback >= 0) {
            log.info("用户冻结余额恢复成功 >>> {} ", rollback);
        } else {
            log.info("用户冻结余额恢复失败,即将重试");
        }
        return getHmilyTccAccount(accountDTO);
    }


    /**
     * 冻结金额
     *
     * @param accountDTO
     */
    private HmilyTccAccount decrease(AccountDTO accountDTO) {
        int decrease = accountMapper.update(accountDTO);
        if (decrease <= 0) {
            throw new RuntimeException(accountDTO.getUserId() + ">> 用户,扣减" + accountDTO.getAmount().doubleValue() + "失败");
        }
        HmilyTccAccount hmilyTccAccount = getHmilyTccAccount(accountDTO);
        return hmilyTccAccount;
    }

    private HmilyTccAccount getHmilyTccAccount(AccountDTO accountDTO) {
        QueryWrapper<HmilyTccAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(HmilyTccAccount::getUserId, accountDTO.getUserId());
        HmilyTccAccount hmilyTccAccount = accountMapper.selectOne(queryWrapper);
        return hmilyTccAccount;
    }

    private AccountDTO buildAccountDTO(AccountNestedDTO nestedDTO) {
        AccountDTO dto = new AccountDTO();
        dto.setAmount(nestedDTO.getAmount());
        dto.setUserId(nestedDTO.getUserId());
        return dto;
    }

    private InventoryDTO buildInventoryDTO(AccountNestedDTO nestedDTO) {
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setCount(nestedDTO.getCount());
        inventoryDTO.setProductId(nestedDTO.getProductId());
        return inventoryDTO;
    }
}
