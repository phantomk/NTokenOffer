### NTOKEN自动报价使用说明

[toc]

#### 启动配置

1. 准备好：钱包私钥、以太坊节点URL、etherscan-api-key、ERC20代币合约地址、获取价格API。

   * 钱包私钥：通过助记词生成，可通过nestDapp注册。报价至少需要准备10.2ETH和价值10ETH的ERC20代币。

   * 以太坊节点URL：可通过https://infura.io 免费申请。

   * etherscan-api-key：可通过https://cn.etherscan.com免费申请。

   * ERC20代币合约地址：例如报价HT，那么就需要填写HT代币合约地址。

   * 价格API：目前脚本仅支持火币交易所API支持的token，其他token可以自己重写脚本里面的获取价格接口。

     | currency |              contract-address              |                            price-api                             |
     | :------: | :----------------------------------------: | :--------------------------------------------------------------: |
     |    HT    | 0x6f259637dcd74c767781e37bc6133cd6a68aa161 |  https://api.huobi.pro/market/history/trade?symbol=hteth&size=1  |
     |   OMG    | 0xd26114cd6EE289AccF82350c8d8487fedB8A0C07 | https://api.huobi.pro/market/history/trade?symbol=omgeth&size=1  |
     |   BAT    | 0x0d8775f648430679a709e98d2b0cb6250d2887ef | https://api.huobi.pro/market/history/trade?symbol=bateth&size=1  |
     |   LINK   | 0x514910771af9ca656af840dff83e8264ecf986ca | https://api.huobi.pro/market/history/trade?symbol=linketh&size=1 |
     |   ZRX    | 0xe41d2489571d322189246dafa5ebde1f4699f498 | https://api.huobi.pro/market/history/trade?symbol=zrxeth&size=1  |
     |   MCO    | 0xb63b606ac810a52cca15e44bb630fd42d8d1d83d | https://api.huobi.pro/market/history/trade?symbol=mcoeth&size=1  |

    * 交易所一般没有 BTC-ETH 的交易对，所有获取价格数据可以用 ETH-BTC。 
     | currency |              contract-address              |                            price-api                            |
     | :------: | :----------------------------------------: | :-------------------------------------------------------------: |
     |   HBTC   | 0x0316EB71485b0Ab14103307bf65a021042c6d380 | https://api.huobi.pro/market/history/trade?symbol=ethbtc&size=1 |
    
    * **注意如果使用 ETH—BTC 对，需要注意要修改脚本中对应的代码保证价格正确**
      ```java
      @Override
      public void updatePrice() {
        BigDecimal exchangePrice = getExchangePrice();

        // NOTE: 需要把这行注释掉，保证价格不是反向的
        exchangePrice = new BigDecimal("1").divide(exchangePrice,18,BigDecimal.ROUND_DOWN);
        ...
      }
      ```
  
   1. 在 application.properties 里面根据注释将数据填写在对应的地方。

   #### 项目运行后操作

   1. 授权
      * 运行后会优先检测是否对nToken报价合约授权，如果授权金额不够，那么会发起授权交易。
   2. 打开 http://ip:8088/offer/mingData 页面，点击：修改状态。

   

   