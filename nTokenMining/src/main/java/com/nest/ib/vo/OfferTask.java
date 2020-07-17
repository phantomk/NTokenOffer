package com.nest.ib.vo;

import com.nest.ib.service.MiningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

/**
 * ClassName:applicationBonusStorage
 * Description:
 */
@Component
public class OfferTask {

    @Autowired
    private MiningService miningService;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(50);
        return taskScheduler;
    }

    /**
    *   报价：ETH/HT
    */
    @Scheduled(fixedDelay = 3000)
    public void offer(){
        miningService.offer();
    }
    /**
    *   取回
    */
    @Scheduled(fixedDelay = 120000)
    public void turnOut(){
        miningService.turnOut();
    }
    /**
    *   更新ETH/ERC20价格
    */
    @Scheduled(fixedDelay = 5000)
    public void updatePrice(){
        miningService.updatePrice();
    }
    /**
    *   检测账户余额是否足够报价
    */
    @Scheduled(fixedDelay = 20000)
    public void checkWalletBalance(){
        miningService.checkWalletBalance();
    }

}
