package com.nest.ib.controller;

import com.nest.ib.service.MiningService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.web3j.crypto.Credentials;

/**
 * ClassName:MiningController
 * Description:
 */
@RestController
@RequestMapping("/offer")
public class MiningController {

    @Autowired
    private MiningService miningService;

    /**
    * 开启/关闭挖矿. true开启,false关闭
    */
    @PostMapping("/startMining")
    public boolean startMining(){
        miningService.startMining();
        return miningService.selectStartMining();
    }
    /**
    * 设置区块间隔数量
    */
    @PostMapping("/updateIntervalBlock")
    public int updateIntervalBlock(@RequestParam(name = "intervalBlock") int intervalBlock){
        miningService.updateIntervalBlock(intervalBlock);
        return miningService.selectIntervalBlock();
    }
    /**
     * 设置账户私钥
     */
    @PostMapping("/updatePrivateKey")
    public String updateUserPrivatekey(@RequestParam(name = "privateKey") String privateKey){
        miningService.updateUserPrivateKey(privateKey);
        return miningService.selectUserWalletAddress();
    }
    /**
    * 查看矿机详情
    */
    @GetMapping("/miningData")
    public ModelAndView miningData(){
        String address = miningService.selectUserWalletAddress();
        if(address == null)address = "请先填写正确的私钥";
        int intervalBlock = miningService.selectIntervalBlock();
        boolean b = miningService.selectStartMining();
        ModelAndView mav = new ModelAndView("miningData");
        mav.addObject("address",address);
        mav.addObject("intervalBlock",intervalBlock);
        mav.addObject("startMining",b == true ? "矿机状态: 开启" : "矿机状态: 关闭");
        return mav;
    }


}
