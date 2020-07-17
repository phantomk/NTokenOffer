package com.nest.ib.service.serviceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nest.ib.contract.*;
import com.nest.ib.service.MiningService;
import com.nest.ib.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.Contract;
import org.web3j.utils.Numeric;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * ClassName:MiningServiceImpl
 * Description:
 */
@Service
public class MiningServiceImpl implements MiningService {
    private static final Logger LOG = LoggerFactory.getLogger(MiningServiceImpl.class);
    // 报价调用的方法名
    private static final String INPUT_OFFER = "0xf6a4932f";
    // 取回操作topic记录
    private static final String TRANSACTION_TOPICS_CONTRACT = "0xb3ed50358b669287aff082dbd5407bd75efcac18c9267d946074dc90d8aae898";
    // 是否开启挖矿
    private static boolean START_MINING = false;
    // 报价工厂合约地址
    private static String OFFER_FACTORY_CONTRACT_ADDRESS = null;
    // 报价价格合约地址
    private static String OFFER_PRICE_CONTRACT_ADDRESS = null;
    // nToken代币合约地址
    private static String NTOKEN_CONTRACT_ADDRESS = null;
    // 区块间隔数量,默认60
    private static int DEFAULT_BLOCK_INTERVAL = 60;
    private static final BigDecimal UNIT_ETH = new BigDecimal("1000000000000000000");
    private static BigDecimal UNIT_ERC20 = null;
    // 报价的gasPrice的默认倍数
    private static BigDecimal OFFER_GAS_PRICE_MULTIPLE = new BigDecimal("1.4");
    // 取回的gasPrice的默认倍数
    private static BigDecimal TURNOUT_GAS_PRICE_MULTIPLE = new BigDecimal("1.2");
    // 报价需要的USDT数量
    private static BigInteger OFFER_ERC20_AMOUNT = null;
    // 报价需要的ETH数量(wei)
    private static BigInteger OFFER_ETH_AMOUNT = new BigInteger("10000000000000000000");
    // 完成报价交易需要打入合约里面的ETH数量
    private static BigInteger PAYABLE_ETH_AMOUNT = new BigInteger("10100000000000000000");
    // 报价ETH数量（ether）
    private static final BigDecimal ETH_AMOUNT = new BigDecimal("10");
    // 账户余额是否足够
    private static boolean USER_WALLET_BALANCE_ENOUGH = false;

    @Value("${private.key}")
    private String USER_PRIVATE_KEY;
    @Value("${eth.node}")
    private String ETH_NODE;
    @Value("${etherscan.api.key}")
    private String ETHERSCAN_API_KEY_TOKEN;
    @Value("${erc20.token.address}")
    private String ERC20_TOKEN_ADDRESS;
    @Value("${erc20.eth.price}")
    private String URL_ERC20_ETH_PRICE;

    /**
     * 开启报价
     */
    @Override
    public void offer() {
        // 检查是否设置了开启矿机以及所有合约地址是否都已经获取
        if (!START_MINING || !USER_WALLET_BALANCE_ENOUGH || OFFER_FACTORY_CONTRACT_ADDRESS == null || OFFER_PRICE_CONTRACT_ADDRESS == null || UNIT_ERC20 == null) {
            LOG.info("还未开启矿机或者合约地址还未全部获取到");
            return;
        }
        Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
        Web3j web3j = Web3j.build(new HttpService(ETH_NODE));
        BigInteger gasPrice;
        BigInteger nonce;
        BigInteger blockNumber;
        try {
            gasPrice = web3j.ethGasPrice().send().getGasPrice();
            nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
            blockNumber = web3j.ethBlockNumber().send().getBlockNumber();
        } catch (IOException e) {
            LOG.error("报价时连接infura出现异常");
            e.printStackTrace();
            return;
        }
        /**
         *   获取最后一笔报价的区块高度
         */
        BigInteger latestOfferBlockNumber = null;
        NTokenContract nTokenContract = NTokenContract.load(NTOKEN_CONTRACT_ADDRESS,web3j,credentials, Contract.GAS_PRICE,Contract.GAS_LIMIT);
        try {
            latestOfferBlockNumber = nTokenContract.checkBlockInfo().sendAsync().get().getValue2();
            LOG.info("最后一笔报价区块高度：" + latestOfferBlockNumber + "   当前区块高度：" + blockNumber);
        } catch (Exception e) {
            LOG.error("调用报价价格合约获取最后一笔报价区块号，出现异常");
            e.printStackTrace();
            return;
        }
        String transactionHash = sendERC20Offer(web3j, credentials, gasPrice, nonce,blockNumber,latestOfferBlockNumber);
        if(transactionHash == null){
            return;
        }
        LOG.info("报价hash为：" + transactionHash);
        while (true){
            try {
                /**
                 *   休眠2秒，是为了防止频繁过度的请求infura，导致快速达到节点限制的访问上限(如果是infura的付费节点，访问次数较多，将该休眠去掉效果更好)
                 */
                Thread.sleep(2000);
                /**
                *   如果nonce改变了，那么说明交易已经打包成功了。或者被其他的交易覆盖掉了
                */
                BigInteger nowNonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
                if(nowNonce.compareTo(nonce) > 0){
                    return;
                }
                /**
                *   如果获取的最后一笔报价的区块号变了，那么要么是别人打包成功了，要么是自己打包成功，此时都发起取消交易。
                *   1. 如果是别人成功了，那么取消交易可以避免被卡区块间隔
                *   2. 如果是自己成功了，由于nonce相同，那么发起的交易为null，没有任何损失
                */
                BigInteger nowLatestBlockNumber = nTokenContract.checkBlockInfo().sendAsync().get().getValue2();
                if(nowLatestBlockNumber.compareTo(latestOfferBlockNumber) > 0){
                    cancelTransaction(nonce,gasPrice.multiply(new BigInteger("2")));
                    return;
                }
                /**
                *   每增长一个区块，还未打包成功，那么在原来gasPrice的基础上，再增加1.1倍
                *   目的：有效防止当gasPrice剧烈波动的时候，无法顺利的打包成功
                */
                BigInteger nowBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
                if(nowBlockNumber.compareTo(blockNumber) > 0){
                    blockNumber = nowBlockNumber;
                    gasPrice = new BigInteger(String.valueOf(new BigDecimal(gasPrice).multiply(new BigDecimal("1.2").setScale(0,BigDecimal.ROUND_DOWN))));
                    String hash = sendERC20Offer(web3j, credentials, gasPrice, nonce, nowBlockNumber, latestOfferBlockNumber);
                    LOG.info("加速报价hash为：" + hash);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 发送ERC20-ETH的报价
     */
    @Override
    public String sendERC20Offer(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger nonce,BigInteger blockNumber,BigInteger latestBlockNumber) {
        gasPrice = new BigInteger(String.valueOf(new BigDecimal(gasPrice).multiply(OFFER_GAS_PRICE_MULTIPLE).setScale(0, BigDecimal.ROUND_DOWN)));
        BigDecimal n = new BigDecimal(String.valueOf(DEFAULT_BLOCK_INTERVAL - 1));
        // 检测是否满足间隔的区块数量
        boolean b = false;
        try {
            BigInteger setBlockNumber = blockNumber.subtract(new BigInteger(String.valueOf(n)));
            if (setBlockNumber.compareTo(new BigInteger(String.valueOf(latestBlockNumber))) > 0) {
                b = true;
            }
            LOG.info("上一笔报价区块高度: " + latestBlockNumber + "  当前区块高度: " + blockNumber + "   gasPrice: " + gasPrice);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (b) {
            /**
             *   获取当前合约里面最新有效价格，跟当前准备报价的价格对比，如果偏离超过10%，那么10倍报价
             */
            NestOfferPriceContract nestOfferPriceContract = NestOfferPriceContract.load(OFFER_PRICE_CONTRACT_ADDRESS,web3j,credentials,Contract.GAS_PRICE,Contract.GAS_LIMIT);
            Tuple2<BigInteger, BigInteger> bigIntegerBigIntegerBigIntegerTuple2;
            try {
                bigIntegerBigIntegerBigIntegerTuple2 = nestOfferPriceContract.checkPriceNow(ERC20_TOKEN_ADDRESS).sendAsync().get();
            } catch (Exception e) {
                LOG.error("通过价格合约获取USDT价格出现异常");
                e.printStackTrace();
                return null;
            }
            // 价格合约里面最新的有效价格
            BigDecimal otherMinerOfferPrice = new BigDecimal(bigIntegerBigIntegerBigIntegerTuple2.getValue2()).divide(UNIT_ERC20,18,BigDecimal.ROUND_DOWN)
                    .divide(new BigDecimal(bigIntegerBigIntegerBigIntegerTuple2.getValue1()).divide(UNIT_ETH,18,BigDecimal.ROUND_DOWN),18,BigDecimal.ROUND_DOWN);
            // 当前交易所的价格
            BigDecimal companyOfferPrice = new BigDecimal(OFFER_ERC20_AMOUNT).divide(UNIT_ERC20,18,BigDecimal.ROUND_DOWN)
                    .divide(new BigDecimal(OFFER_ETH_AMOUNT).divide(UNIT_ETH,18,BigDecimal.ROUND_DOWN),18,BigDecimal.ROUND_DOWN);
            // 报价需要支付的手续费
            BigDecimal serviceCharge = new BigDecimal(OFFER_ETH_AMOUNT).multiply(new BigDecimal("0.01")).setScale(0, BigDecimal.ROUND_DOWN);
            // 报价需要打入到合约里面的ETH数量
            BigInteger payableEth = OFFER_ETH_AMOUNT.add(new BigInteger(String.valueOf(serviceCharge)));
            // 报价ETH数量
            BigInteger offerEthAmount = OFFER_ETH_AMOUNT;
            // 报价USDT数量
            BigInteger offerErc20Amount = OFFER_ERC20_AMOUNT;
            if(otherMinerOfferPrice.multiply(new BigDecimal("1.1")).compareTo(companyOfferPrice) < 0
                    || otherMinerOfferPrice.multiply(new BigDecimal("0.9")).compareTo(companyOfferPrice) > 0){
                offerEthAmount = OFFER_ETH_AMOUNT.multiply(new BigInteger("10"));
                offerErc20Amount = OFFER_ERC20_AMOUNT.multiply(new BigInteger("10"));
                payableEth = offerEthAmount.add(new BigInteger(String.valueOf(serviceCharge)));
            }
            LOG.info("报价ETH数量：" + offerEthAmount + " 报价USDT数量：" + offerErc20Amount + "  打入合约ETH数量：" + payableEth);
            Function function = new Function(
                    "offer",
                    Arrays.<Type>asList(
                            new Uint256(offerEthAmount),
                            new Uint256(new BigInteger(String.valueOf(offerErc20Amount))),
                            new Address(ERC20_TOKEN_ADDRESS)),
                    Collections.<TypeReference<?>>emptyList());
            String encode = FunctionEncoder.encode(function);
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    new BigInteger("1200000"),
                    OFFER_FACTORY_CONTRACT_ADDRESS,
                    payableEth,
                    encode);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            String transactionHash = null;
            try {
                transactionHash = web3j.ethSendRawTransaction(hexValue).sendAsync().get().getTransactionHash();
            } catch (Exception e) {
                LOG.error("报价时发送交易出现异常");
                e.printStackTrace();
                return null;
            }
            return transactionHash;
        }
        return null;
    }

    /**
     *  取回报价资产
     */
    @Override
    public void turnOut() {
        if(OFFER_FACTORY_CONTRACT_ADDRESS == null){
            return;
        }
        List<String> offerContractAddresses = getOfferContractAddress();
        if(offerContractAddresses.size() == 0){
            LOG.info("目前没有需要取回的报价合约");
            return;
        }
        Web3j web3j = Web3j.build(new HttpService(ETH_NODE));
        BigInteger gasPrice = null;
        try {
            gasPrice = web3j.ethGasPrice().send().getGasPrice();
        } catch (IOException e) {
            LOG.error("取回时获取gasPrice出现异常：" + e);
            return;
        }
        gasPrice = new BigInteger(String.valueOf(new BigDecimal(gasPrice).multiply(TURNOUT_GAS_PRICE_MULTIPLE).setScale(0,BigDecimal.ROUND_DOWN)));
        Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
        BigInteger nonce = null;
        try {
            nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
        } catch (IOException e) {
            LOG.error("取回时获取nonce出现异常：" + e);
            return;
        }
        /**
        *   遍历所有未取回的合约，进行批量取回
        */
        for(String offerContractAddress : offerContractAddresses){
            BigInteger gasLimit = new BigInteger("500000");
            Function function = new Function(
                    "turnOut",
                    Arrays.<Type>asList(new Address(offerContractAddress)),
                    Collections.<TypeReference<?>>emptyList());
            String encode = FunctionEncoder.encode(function);
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    OFFER_FACTORY_CONTRACT_ADDRESS,
                    encode);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction,credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            String transactionHash = null;
            try {
                transactionHash = web3j.ethSendRawTransaction(hexValue).sendAsync().get().getTransactionHash();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(transactionHash == null){
                LOG.info("取回报价出现异常");
                return;
            }
            nonce = nonce.add(new BigInteger("1"));
            LOG.info("取回hash： " + transactionHash);
        }
    }
    /**
    *   检查钱包ETH和USDT余额，是否足够报价，如果不够，则报价无法启动
    */
    @Override
    public void checkWalletBalance() {
        if(OFFER_ERC20_AMOUNT == null){
            return;
        }
        Web3j web3j = Web3j.build(new HttpService(ETH_NODE));
        Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
        // 检测ETH,USDT余额是否
        BigInteger ethBalance = null;
        try {
            ethBalance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
        } catch (IOException e) {
            LOG.error("报时获取账户ETH余额出现异常");
            e.printStackTrace();
            return;
        }
        BigInteger usdtBalance = null;
        try {
            usdtBalance = ERC20.load(ERC20_TOKEN_ADDRESS, web3j, credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT).balanceOf(credentials.getAddress()).send();
        } catch (Exception e) {
            LOG.error("报价时获取账户USDT余额出现异常");
            e.printStackTrace();
            return;
        }
        if(ethBalance.compareTo(PAYABLE_ETH_AMOUNT) <= 0 || usdtBalance.compareTo(OFFER_ERC20_AMOUNT) < 0){
            LOG.info("账户余额不足，请检查是否有未取回的合约！");
            USER_WALLET_BALANCE_ENOUGH = false;
        }else {
            USER_WALLET_BALANCE_ENOUGH = true;
        }
    }
    /**
     *  取消交易（使用跟报价相同的nonce，设置gasPrice比报价高，给自己进行一笔转账，覆盖报价交易）
     */
    @Override
    public String cancelTransaction(BigInteger nonce,BigInteger gasPrice) {
        Web3j web3j = Web3j.build(new HttpService(ETH_NODE));
        Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
        BigInteger value = new BigInteger("0");
        try{
            BigInteger gasLimit = new BigInteger("200000");
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, credentials.getAddress(), value);
            byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            String transactionHash = ethSendTransaction.getTransactionHash();
            LOG.info("取消交易hash：" + transactionHash);
            return transactionHash;
        }catch (Exception e){
            return null;
        }
    }
    /**
    * 更新区块间隔数量
    */
    @Override
    public void updateIntervalBlock(int blockInterval) {
        DEFAULT_BLOCK_INTERVAL = blockInterval;
    }
    /**
     *  查看当前的区块间隔数量
     */
    @Override
    public int selectIntervalBlock() {
        return DEFAULT_BLOCK_INTERVAL;
    }
    /**
     *  启动/关闭挖矿. true启动,false关闭
     */
    @Override
    public void startMining() {
        START_MINING = START_MINING == true ? false : true;
    }
    /**
     *  查看当前矿机是否是启动状态
     */
    @Override
    public boolean selectStartMining() {
        return START_MINING;
    }
    /**
     *  更新用户私钥
     */
    @Override
    public void updateUserPrivateKey(String privateKey){
        USER_PRIVATE_KEY = privateKey;
    }
    /**
     *  查看用户钱包地址
     */
    @Override
    public String selectUserWalletAddress(){
        return USER_PRIVATE_KEY.equalsIgnoreCase("") ? null : Credentials.create(USER_PRIVATE_KEY).getAddress();
    }
    /**
    *   更新报价工厂合约地址
    */
    @Override
    public void updateOfferFactoryContractAddress(String offerFactoryContractAddress) {
        if(offerFactoryContractAddress != null){
            OFFER_FACTORY_CONTRACT_ADDRESS = offerFactoryContractAddress;
            System.out.println("报价工厂合约地址更新：" + OFFER_FACTORY_CONTRACT_ADDRESS);
        }
    }
    /**
    *   更新报价价格合约地址
    */
    @Override
    public void updateOfferPriceContractAddress(String offerPriceContractAddress) {
        if(offerPriceContractAddress != null){
            OFFER_PRICE_CONTRACT_ADDRESS = offerPriceContractAddress;
            System.out.println("报价价格合约地址更新：" + OFFER_PRICE_CONTRACT_ADDRESS);
        }
    }
    /**
     *   更新nToken代币合约地址
     */
    @Override
    public void updateNTokenContractAddress(String nTokenContractAddress) {
        if(nTokenContractAddress != null){
            NTOKEN_CONTRACT_ADDRESS = nTokenContractAddress;
            System.out.println("nToken代币合约地址更新：" + NTOKEN_CONTRACT_ADDRESS);
        }
    }
    /**
    *   更新ERC20代币的精度
    */
    @Override
    public void updateErc20Decimal(BigInteger decimal){
        if(decimal != null){
            UNIT_ERC20 = new BigDecimal(Math.pow(10,decimal.intValue())).setScale(0,BigDecimal.ROUND_DOWN);
        }
    }

    /**
    *   调用火币API获取价格，计算出指定ETH报价需要的USDT数量
    */
    @Override
    public void updatePrice() {
        BigDecimal exchangePrice = getExchangePrice();
        exchangePrice = new BigDecimal("1").divide(exchangePrice,18,BigDecimal.ROUND_DOWN);
        if(exchangePrice == null){
            LOG.info("访问火币交易所API失败");
            return;
        }
        BigDecimal usdtAmount = exchangePrice.multiply(UNIT_ERC20).setScale(0,BigDecimal.ROUND_DOWN);
        usdtAmount = usdtAmount.multiply(ETH_AMOUNT);
        /**
        *   不允许跟上一个价格偏移超越20%
        */
        if(OFFER_ERC20_AMOUNT != null){
            if(usdtAmount.compareTo(new BigDecimal(OFFER_ERC20_AMOUNT).multiply(new BigDecimal("1.2"))) > 0 || usdtAmount.compareTo(new BigDecimal(OFFER_ERC20_AMOUNT).multiply(new BigDecimal("0.8"))) < 0){
                LOG.info("交易所价格出现了超过20%的偏差，已停止运行，请立即检查是否出错");
                START_MINING = false;
                return;
            }
        }
        OFFER_ERC20_AMOUNT = new BigInteger(String.valueOf(usdtAmount.setScale(0,BigDecimal.ROUND_DOWN)));
        LOG.info("更新价格 --> 报价需要ETH数量为：" + OFFER_ETH_AMOUNT + "  ERC20数量为：" + OFFER_ERC20_AMOUNT);
    }
    /**
    * 获取交易所价格
    */
    @Override
    public BigDecimal getExchangePrice(){
        String s;
        try {
            s = HttpClientUtil.sendHttpGet(URL_ERC20_ETH_PRICE);
        }catch (Exception e){
            return null;
        }
        if(s == null)return null;
        JSONObject jsonObject = JSONObject.parseObject(s);
        BigDecimal price = JSONObject.parseObject(
                String.valueOf(
                        JSONObject.parseObject(
                                String.valueOf(
                                        jsonObject.getJSONArray("data").get(0)
                                )
                        ).getJSONArray("data").get(0)
                )
        ).getBigDecimal("price");
        return price == null ? null : price.setScale(6,BigDecimal.ROUND_DOWN);
    }
    /**
     * 获取需要取回的报价合约地址
     * @return
     */
    private List<String> getOfferContractAddress() {
        List<String> offerContractAddresses = new ArrayList<>();
        String url = "https://api-cn.etherscan.com/api?module=account&action=txlist&address=" + OFFER_FACTORY_CONTRACT_ADDRESS + "&startblock=0&endblock=99999999&page=1&offset=300&sort=desc&apikey=" + ETHERSCAN_API_KEY_TOKEN;
        String s = HttpClientUtil.sendHttpGet(url);
        JSONArray resultEtherscan = JSONObject.parseObject(s).getJSONArray("result");
        Web3j web3j = Web3j.build(new HttpService(ETH_NODE));
        BigInteger nowBlockNumber;
        try {
             nowBlockNumber = web3j.ethBlockNumber().sendAsync().get().getBlockNumber();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return offerContractAddresses;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return offerContractAddresses;
        }
        Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
        for(int i=0; i<resultEtherscan.size(); i++) {
            Object o = resultEtherscan.get(i);
            JSONObject jsonObject1 = JSONObject.parseObject(String.valueOf(o));
            String from = jsonObject1.getString("from");
            String isError = jsonObject1.getString("isError");
            String input = jsonObject1.getString("input");
            String hash = jsonObject1.getString("hash");
            int blockNumber = jsonObject1.getIntValue("blockNumber");
            if(nowBlockNumber.intValue() - blockNumber <= 25){
                continue;
            }
            if (input.length() < 10) {
                continue;
            }
            if (input.substring(0, 10).equalsIgnoreCase(INPUT_OFFER) && from.equalsIgnoreCase(credentials.getAddress()) && isError.equalsIgnoreCase("0")) {
                EthGetTransactionReceipt ethGetTransactionReceipt = null;
                try {
                    ethGetTransactionReceipt = web3j.ethGetTransactionReceipt(hash).sendAsync().get();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                TransactionReceipt result = ethGetTransactionReceipt.getResult();
                List<Log> logs = result.getLogs();
                if (logs.size() == 0) {
                    return null;
                }
                // 遍历当前transactionHash下所有的日志记录
                for (Log log : logs) {
                    List<String> topics = log.getTopics();
                    String address = log.getAddress();
                    if (!address.equalsIgnoreCase(OFFER_FACTORY_CONTRACT_ADDRESS)) {
                        continue;        // 确定一定要是报价工厂合约地址,才能继续往下执行
                    }
                    // 如果有报价记录
                    if (topics.get(0).equalsIgnoreCase(TRANSACTION_TOPICS_CONTRACT)) {
                        String data = log.getData();
                        String contractAddress = "0x" + data.substring(26, 66);  // 报价合约地址
                        // 检查是否领取过
                        Nest3OfferMain nest3OfferMain = Nest3OfferMain.load(OFFER_FACTORY_CONTRACT_ADDRESS, web3j, credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT);
                        BigInteger toIndex = null;
                        String offerData = null;
                        try {
                            toIndex = nest3OfferMain.toIndex(contractAddress).sendAsync().get();
                            offerData = nest3OfferMain.getPrice(toIndex).sendAsync().get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        String[] split = offerData.split(",");
                        // 剩余可成交ETH数量
                        BigInteger ethAmount = new BigInteger(split[3]);
                        // 剩余可成交ERC20数量
                        BigInteger erc20Amount = new BigInteger(split[4]);
                        System.out.println("合约地址：" + contractAddress + "  ETH：" + ethAmount + "  ERC20: " + erc20Amount);
                        if (ethAmount.compareTo(new BigInteger("0"))>0 || erc20Amount.compareTo(new BigInteger("0"))>0) { // 如果还未领取
                            offerContractAddresses.add(contractAddress);
                        }
                    }
                }
            }
        }
        return offerContractAddresses;
    }
}
