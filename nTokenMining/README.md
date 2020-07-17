[toc]

***

### NEST 3.0 nToken-ETH自动报价（以HT报价为例）

***

##### 1. 创建私钥，节点，etherscan-api-key

>由于调用合约获取相关数据以及发送交易，需要跟链交互，需要准备一个以太坊节点URL和私钥，节点可以通过https://infura.io/ 注册后免费申请。
>
>报价后的资产，需要触发合约进行取回，由于没有建立数据库进行数据整理，我们使用的是通过Etherscan的方式来获取所有的报价合约的交易，筛选出需要取回的报价合约。

```java
// 以太坊节点 
String ETH_NODE = "";
// 私钥
String USER_PRIVATE_KEY = "";
Web3j web3j = Web3j.build(new HttpService(ETH_NODE));
Credentials credentials = Credentials.create(USER_PRIVATE_KEY);
```

##### 2. 获取Nest Protocol相关合约地址

>Nest  Protocol中映射合约的作用：管理其它所有合约地址。
>
>报价涉及到的合约为：HT代币合约，映射合约，报价合约，矿池合约，价格合约。

```java
// NEST映射合约地址
String mappingContractAddress = "";
// 创建映射合约对象
MappingContract mappingContract = MappingContract.load(mappingContractAddress,web3j,credentials,GAS_PRICE,GAS_LIMIT);
// 报价合约地址
String offerContractAddress = mappingContract.checkAddress("nest.nToken.offerMain").sendAsync().get();
// 价格合约地址
String offerPriceContractAddress = mappingContract.checkAddress("nest.v3.offerPrice").sendAsync().get();
// nToken映射合约
String nTokenMappingContractAddress = mappingContract.checkAddress("nest.nToken.tokenMapping").sendAsync().get();
// nToken代币合约地址
String nTokenContactAddress = NTokenMapping.load(nTokenMappingContractAddress, web3j, credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT).checkTokenMapping(ERC20_TOKEN_ADDRESS).sendAsync().get();
```

##### 3. 授权报价合约HT

>报价需要将HT转入到报价合约，转HT操作是由报价合约调用HT代币合约来执行，故需要对报价合约进行HT授权。

```java
// 报价合约地址
String offerContractAddress = "";
// 设置gasPrice为默认的2倍，可自行调整
BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice().multiply(new BigInteger("2"));
// 创建ERC20代币合约对象
ERC20 erc20 = ERC20.load(HT_TOKEN_ADDRESS, web3j, credentials, gasPrice, new BigInteger("200000"));
// 获取对报价合约的USDT授权金额
BigInteger approveValue = erc20.allowance(credentials.getAddress(), offerContractAddress).sendAsync().get();
// 采用一次性授权方式，如果授权金额小于1亿USDT，即授权99999999999999.999999USDT
if(approveValue.compareTo(new BigInteger("100000000000000")) < 0){
      String transactionHash = erc20.approve(offerContractAddress, new BigInteger("99999999999999999999")).sendAsync().get().getTransactionHash();
      System.out.println("一次性授权hash：" + transactionHash);
}
```

##### 4. 设置挖矿区块间隔

>通过调用nToken代币合约获取最后一笔报价的区块高度，通过链接节点获取最新的区块高度，相减即可得到间隔上一笔报价的间隔区块数量。
>

```java
// nToken代币合约地址
String nTokenContactAddress = "";
// nToken代币合约对象
NTokenContract nTokenContract = NTokenContract.load(NTOKEN_CONTRACT_ADDRESS,web3j,credentials, Contract.GAS_PRICE,Contract.GAS_LIMIT);
// 最后一笔报价的区块高度
BigInteger latestOfferBlockNumber = nTokenContract.checkBlockInfo().sendAsync().get().getValue2();
// 当前区块高度
BigInteger nowBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
// 获取挖矿区块间隔数量
BigInteger intervalBlockNumber = nowBlockNumber.subtract(latestOfferBlockNumber);
```

##### 5. 设置报价ERC20和ETH数量

>通过交易频率很高的交易所API获取ERC20-ETH价格，进行计算获取10ETH报价需要的ERC20和ETH数量即可。如果价格偏离超过10%需要进行10倍报价，或者是进行多倍ETH报价，只需要在报价的时候乘以对应的倍数就行。
>
>部分交易所API需要海外节点才能访问，以下采用的是火币交易所API：

```java
// HT精度
BigDecimal UNIT_HT = new BigDecimal("1000000000000000000");
// 火币ht-ETH的API
String erc20EthPriceUrl = "https://api.huobi.pro/market/history/trade?symbol=hteth&size=1";
// 访问火币API获取的数据
String s;
try {
	s = HttpClientUtil.sendHttpGet(erc20EthPriceUrl);
}catch (Exception e){
	return null;
}
if(s == null){
	return null;
}
// 将获取的字符串转换为json
JSONObject jsonObject = JSONObject.parseObject(s);
// 筛选出ETH/USDT价格
BigDecimal price = JSONObject.parseObject(
	String.valueOf(
		JSONObject.parseObject(
			String.valueOf(
				jsonObject.getJSONArray("data").get(0)
			)
		).getJSONArray("data").get(0)
	)
).getBigDecimal("price");
// 10ETH报价需要的ETH数量
BigDecimal ethAmount = new BigDecimal("10");
// 10ETH报价需要的HT数量
BigDecimal htAmount = exchangePrice.multiply(UNIT_HT.multiply(ethAmount).setScale(0,BigDecimal.ROUND_DOWN);
```

##### 6. 发起报价交易

>如果当前报价的ETH/HT价格与价格合约里面的最新生效价格偏离超过10%，那么报价资产需要10倍，报价支付的手续费不变（不需要10倍）。
>
>当设置的间隔区块满足要求，并且报价需要的ETH和HT数量都已经计算出来，那么此时可以直接发起报价。

```java
// 报价需要的ETH数量
BigInteger ethAmount = "";
// 报价需要的HT数量
BigInteger htAmount = "";
// HT代币合约地址
String htTokenAddress = "";
// 报价合约地址
String offerContractAddress = "";
// 报价价格合约地址
String offerPriceContractAddress = "";
// 获取价格合约对象
NestOfferPriceContract nestOfferPriceContract = NestOfferPriceContract.load(offerPriceContractAddress,web3j,credentials,Contract.GAS_PRICE,Contract.GAS_LIMIT);
// 获取HT-ETH报价的最新生效价格(报价后过了25个区块没有被其他人置换资产)
Tuple2<BigInteger, BigInteger> bigIntegerBigIntegerBigIntegerTuple2 = nestOfferPriceContract.checkPriceNow(htTokenAddress).sendAsync().get();
// 最新生效价格
BigDecimal takeEffectPrice = new BigDecimal(bigIntegerBigIntegerBigIntegerTuple2.getValue2()).divide(UNIT_HT,18,BigDecimal.ROUND_DOWN).divide(new BigDecimal(bigIntegerBigIntegerBigIntegerTuple2.getValue1()).divide(UNIT_ETH,18,BigDecimal.ROUND_DOWN),18,BigDecimal.ROUND_DOWN);
// 准备报价的价格
BigDecimal offerPrice = new BigDecimal(htAmount).divide(UNIT_HT,18,BigDecimal.ROUND_DOWN).divide(new BigDecimal(ethAmount).divide(UNIT_ETH,18,BigDecimal.ROUND_DOWN),18,BigDecimal.ROUND_DOWN);
// 报价需要支付的手续费(1%)
BigDecimal serviceCharge = new BigDecimal(ethAmount).multiply(new BigDecimal("0.01")).setScale(0, BigDecimal.ROUND_DOWN);
// 报价需要打入到合约里面的ETH数量
BigInteger payableEth = ethAmount.add(new BigInteger(String.valueOf(serviceCharge)));
// 如果偏离超过10%，那么报价资产需要10倍
if(takeEffectPrice.multiply(new BigDecimal("1.1")).compareTo(offerPrice) < 0 || takeEffectPrice.multiply(new BigDecimal("0.9")).compareTo(offerPrice) > 0){
	ethAmount = ethAmount.multiply(new BigInteger("10"));
	htAmount = htAmount.multiply(new BigInteger("10"));
    payableEth = ethAmount.add(new BigInteger(String.valueOf(serviceCharge)));
}
// 发送报价交易
Function function = new Function(
	"offer",
	Arrays.<Type>asList(
		new Uint256(ethAmount),
    new Uint256(new BigInteger(String.valueOf(usdtAmount))),
    new Address(usdtTokenAddress)),
    Collections.<TypeReference<?>>emptyList());
String encode = FunctionEncoder.encode(function);
RawTransaction rawTransaction = RawTransaction.createTransaction(
    nonce,
    gasPrice,
    new BigInteger("1200000"),
    offerContractAddress,
    payableEth,
    encode);
byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
String hexValue = Numeric.toHexString(signedMessage);
String transactionHash = web3j.ethSendRawTransaction(hexValue).sendAsync().get().getTransactionHash();
```

##### 7. 获取自己报价但没有取回资产的报价合约

>如果”4“中设置挖矿区块间隔，采用的是”方式二“，那么只需要找出离当前区块高度超过25的报价合约地址即可。
>
>如果”4“中设置挖矿区块间隔，采用的是”方式一“，那么可以通过etherscan api拿到需要取回的报价合约地址。
>
>以下采用的是通过etherscan的方式获取需要取回的报价合约地址，需要提前注册etherscan账号申请API-KEY。

```java
// 报价合约地址
String offerContractAddress = "";
// etherscan API-KEY
String etherscanApiKey = "";
// 报价调用的方法名
String INPUT_OFFER = "0xf6a4932f";
// 报价的topic是固定的
String transactionTopic = "0xb3ed50358b669287aff082dbd5407bd75efcac18c9267d946074dc90d8aae898";
// 调用的etherscan api
String etherscanApi = "https://api-cn.etherscan.com/api?module=account&action=txlist&address=" + offerContractAddress + "&startblock=0&endblock=99999999&page=1&offset=200&sort=desc&apikey=" + etherscanApiKey;
String s = HttpClientUtil.sendHttpGet(url);
// 筛选出需要的数据
JSONArray result = JSONObject.parseObject(s).getJSONArray("result");
// 获取当前区块高度
BigInteger nowBlockNumber = web3j.ethBlockNumber().sendAsync().get().getBlockNumber();
for(int i=0; i<result.size(); i++) {
    JSONObject data = JSONObject.parseObject(String.valueOf(result.get(i)));
	String from = data.getString("from");
	String isError = data.getString("isError");
	String input = data.getString("input");
	String hash = data.getString("hash");
	int blockNumber = data.getIntValue("blockNumber");
    // 如果交易距离当前小于26个区块，那么还不能取回
	if(nowBlockNumber.intValue() - blockNumber <= 25){
		continue;
	}
    // 如果input长度小于10，那么认为发起的不是报价交易
	if (input.length() < 10) {
		continue;
	}
    // 第一步筛选：方法名符合，发起人是自己，同时该交易成功打包
	if (input.substring(0, 10).equalsIgnoreCase(INPUT_OFFER) && from.equalsIgnoreCase(credentials.getAddress()) && isError.equalsIgnoreCase("0")) {
    // 获取交易里面的日志信息
    List<Log> logs = web3j.ethGetTransactionReceipt(hash).sendAsync().get().getResult().getLogs();
    // 如果日志为空不需要处理
    if (logs.size() == 0) {
		return;
	}
    for (Log log : logs) {
		String address = log.getAddress();
        // 第二次筛选：地址必须为报价合约地址
		if (!address.equalsIgnoreCase(offerContractAddress)) {
			continue;        
		}
        List<String> topics = log.getTopics();
		// 如果有报价记录
		if (topics.get(0).equalsIgnoreCase(transactionTopic)) {
            // 报价合约地址
         	String contractAddress = "0x" + log.getData().substring(26,66);
			// 创建报价合约对象
            Nest3OfferMain nest3OfferMain = Nest3OfferMain.load(offerContractAddress, web3j, credentials, Contract.GAS_PRICE, Contract.GAS_LIMIT);
            // 根据自己报价的合约获取合约里面存储的数据角标
            BigInteger toIndex = nest3OfferMain.toIndex(contractAddress).sendAsync().get();
            // 根据合约数据角标，获取剩余的token数量
            String offerData = nest3OfferMain.getPrice(toIndex).sendAsync().get();
			// 剩余ETH数量
			BigInteger ethAmount = new BigInteger(split[3]);
			// 剩余ERC20数量
			BigInteger erc20Amount = new BigInteger(split[4]); 
            // 如果剩余资产都不为0，那么说明还未被取回
			if (ethAmount.compareTo(new BigInteger("0"))>0 || erc20Amount.compareTo(new BigInteger("0"))>0) {
                System.out.println("还未被取回的报价合约地址：" + contractAddress);
			} 
        }
    }   
}
```

##### 8. 取回报价合约资产

>”7“中已经获取了还未取回，已经过了25个区块的报价合约，可以根据报价合约直接进行取回操作。

```java
// 报价合约地址
String offerContractAddress = "";
// 需要取回的报价合约地址
String contractAddress = "";
// 设置取回gasPrice，可以根据自己的需要进行设置
BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice().multiply(new BigInteger("2"));
// 设置nonce
BigInteger nonce = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
// 设置取回gasLimit(如果账户ETH余额不多，可以通过预估的方式获取)
BigInteger gasLimit = new BigInteger("500000");
// 发送取回交易
Function function = new Function(
	"turnOut",
	Arrays.<Type>asList(new Address(offerContractAddress)),
	Collections.<TypeReference<?>>emptyList();
String encode = FunctionEncoder.encode(function);
RawTransaction rawTransaction = RawTransaction.createTransaction(
	nonce,
	gasPrice,
	gasLimit,
	offerContractAddress,
	encode);
byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction,credentials);
String hexValue = Numeric.toHexString(signedMessage);
String transactionHash = web3j.ethSendRawTransaction(hexValue).sendAsync().get().getTransactionHash();
```

##### 9. 取消报价

>报价交易发送出去，但是挖矿间隔区块变得不满足要求时，可以调用取消报价操作。

```java
// 报价时记录nonce值，根据nonce值指定要取消的交易
BigInteger nonce = "";
// 转账ETH金额
BigInteger value = new BigInteger("0");
// 设置gasLimit
BigInteger gasLimit = new BigInteger("200000");
// 通过自己给自己转账的形式，取消掉报价操作
RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, credentials.getAddress(), value);
byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
String hexValue = Numeric.toHexString(signMessage);
EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
String transactionHash = ethSendTransaction.getTransactionHash();
```

