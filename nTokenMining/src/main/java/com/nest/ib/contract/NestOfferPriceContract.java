package com.nest.ib.contract;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;


public class NestOfferPriceContract extends Contract {
    private static final String BINARY = "6080604052662386f26fc10000600355600260045560086005553480156200002657600080fd5b506040516200295b3803806200295b833981810160405260208110156200004c57600080fd5b8101908080519060200190929190505050806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff1660e01b815260040180806020018281038252600c8152602001807f6f66666572466163746f7279000000000000000000000000000000000000000081525060200191505060206040518083038186803b1580156200014157600080fd5b505afa15801562000156573d6000803e3d6000fd5b505050506040513d60208110156200016d57600080fd5b8101908080519060200190929190505050600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff1660e01b81526004018080602001828103825260068152602001807f61626f6e7573000000000000000000000000000000000000000000000000000081525060200191505060206040518083038186803b1580156200026257600080fd5b505afa15801562000277573d6000803e3d6000fd5b505050506040513d60208110156200028e57600080fd5b8101908080519060200190929190505050600760006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505061266b80620002f06000396000f3fe6080604052600436106100c25760003560e01c80638f6f1c591161007f578063a834d32e11610059578063a834d32e146103b9578063ab6118c11461044b578063d4d011b3146104b0578063fd2b451214610520576100c2565b80638f6f1c59146102b0578063915019491461032d578063a781e7f814610368576100c2565b80631892f494146100c75780631b441848146100f957806336af3c811461016c5780634a6238d4146101d15780635d4d3bf7146101fc5780636e873d1f1461026b575b600080fd5b3480156100d357600080fd5b506100dc610582565b604051808381526020018281526020019250505060405180910390f35b34801561010557600080fd5b506101486004803603602081101561011c57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610593565b60405180848152602001838152602001828152602001935050505060405180910390f35b34801561017857600080fd5b506101cf6004803603606081101561018f57600080fd5b810190808035906020019092919080359060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506106af565b005b3480156101dd57600080fd5b506101e6610f19565b6040518082815260200191505060405180910390f35b34801561020857600080fd5b506102696004803603608081101561021f57600080fd5b810190808035906020019092919080359060200190929190803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080359060200190929190505050610f23565b005b34801561027757600080fd5b506102ae6004803603604081101561028e57600080fd5b8101908080359060200190929190803590602001909291905050506111c4565b005b3480156102bc57600080fd5b50610309600480360360408110156102d357600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506112de565b60405180848152602001838152602001828152602001935050505060405180910390f35b34801561033957600080fd5b506103666004803603602081101561035057600080fd5b810190808035906020019092919050505061142d565b005b34801561037457600080fd5b506103b76004803603602081101561038b57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919050505061152d565b005b6103fb600480360360208110156103cf57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050611895565b604051808481526020018381526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001935050505060405180910390f35b34801561045757600080fd5b5061049a6004803603602081101561046e57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050611de8565b6040518082815260200191505060405180910390f35b6104fc600480360360408110156104c657600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080359060200190929190505050611e37565b60405180848152602001838152602001828152602001935050505060405180910390f35b61056c6004803603604081101561053657600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506120b4565b6040518082815260200191505060405180910390f35b600080600454600554915091509091565b60008060003273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16146105d057600080fd5b600260008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160000154600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160010154600260008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600201549250925092509193909250565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff1660e01b815260040180806020018281038252600c8152602001807f6f66666572466163746f7279000000000000000000000000000000000000000081525060200191505060206040518083038186803b15801561075257600080fd5b505afa158015610766573d6000803e3d6000fd5b505050506040513d602081101561077c57600080fd5b810190808051906020019092919050505073ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16146107c457600080fd5b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663cbc7ef096040518163ffffffff1660e01b815260040160206040518083038186803b15801561082e57600080fd5b505afa158015610842573d6000803e3d6000fd5b505050506040513d602081101561085857600080fd5b810190808051906020019092919050505090506000610880824361251e90919063ffffffff16565b90506000600260008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206001016002015490505b818110158061093257506000600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600083815260200190815260200160002060000154145b156109a357600260008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000016000828152602001908152602001600020600201549050600081141561099e576109a3565b6108cd565b600260008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600082815260200190815260200160002060000154600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160000181905550600260008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600082815260200190815260200160002060010154600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160010181905550610b4c86600260008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000160004381526020019081526020016000206000015461253e90919063ffffffff16565b600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600043815260200190815260200160002060000181905550610c0f85600260008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000160004381526020019081526020016000206001015461253e90919063ffffffff16565b600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000160004381526020019081526020016000206001018190555043600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206001016002015414610da057600260008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160020154600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000160004381526020019081526020016000206002018190555043600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600201819055505b3273ffffffffffffffffffffffffffffffffffffffff166006600043815260200190815260200160002060008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a905050507fe3832160127ad742fe92ec88625c8108cc361a8f6931cf6f77ddd4cc7b83a1ea84600260008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160000154600260008873ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160010154604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001838152602001828152602001935050505060405180910390a1505050505050565b6000600354905090565b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff1660e01b815260040180806020018281038252600c8152602001807f6f66666572466163746f7279000000000000000000000000000000000000000081525060200191505060206040518083038186803b158015610fc657600080fd5b505afa158015610fda573d6000803e3d6000fd5b505050506040513d6020811015610ff057600080fd5b810190808051906020019092919050505073ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161461103857600080fd5b6110a184600260008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000160008481526020019081526020016000206000015461251e90919063ffffffff16565b600260008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000160008381526020019081526020016000206000018190555061116483600260008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000160008481526020019081526020016000206001015461251e90919063ffffffff16565b600260008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000160008381526020019081526020016000206001018190555050505050565b600115156000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a3bf06f1336040518263ffffffff1660e01b8152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b15801561126657600080fd5b505afa15801561127a573d6000803e3d6000fd5b505050506040513d602081101561129057600080fd5b81019080805190602001909291905050501515146112ad57600080fd5b600a6112c2828461253e90919063ffffffff16565b146112cc57600080fd5b81600481905550806005819055505050565b60008060003273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161461131b57600080fd5b600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600085815260200190815260200160002060000154600260008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600086815260200190815260200160002060010154600260008873ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000016000878152602001908152602001600020600201549250925092509250925092565b600115156000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a3bf06f1336040518263ffffffff1660e01b8152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b1580156114cf57600080fd5b505afa1580156114e3573d6000803e3d6000fd5b505050506040513d60208110156114f957600080fd5b810190808051906020019092919050505015151461151657600080fd5b6000811161152357600080fd5b8060038190555050565b600115156000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a3bf06f1336040518263ffffffff1660e01b8152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060206040518083038186803b1580156115cf57600080fd5b505afa1580156115e3573d6000803e3d6000fd5b505050506040513d60208110156115f957600080fd5b810190808051906020019092919050505015151461161657600080fd5b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff1660e01b815260040180806020018281038252600c8152602001807f6f66666572466163746f7279000000000000000000000000000000000000000081525060200191505060206040518083038186803b1580156116f957600080fd5b505afa15801561170d573d6000803e3d6000fd5b505050506040513d602081101561172357600080fd5b8101908080519060200190929190505050600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16638fe77e866040518163ffffffff1660e01b81526004018080602001828103825260068152602001807f61626f6e7573000000000000000000000000000000000000000000000000000081525060200191505060206040518083038186803b15801561181757600080fd5b505afa15801561182b573d6000803e3d6000fd5b505050506040513d602081101561184157600080fd5b8101908080519060200190929190505050600760006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b60008060003273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141580156119245750600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614155b1561193857600354341461193757600080fd5b5b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663cbc7ef096040518163ffffffff1660e01b815260040160206040518083038186803b1580156119a257600080fd5b505afa1580156119b6573d6000803e3d6000fd5b505050506040513d60208110156119cc57600080fd5b8101908080519060200190929190505050905060006119f4824361251e90919063ffffffff16565b90506000600260008873ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206001016002015490505b8181101580611aa657506000600260008973ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600083815260200190815260200160002060000154145b15611b1757600260008873ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000160008281526020019081526020016000206002015490506000811415611b1257611b17565b611a41565b600260008873ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600082815260200190815260200160002060000154600260008973ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160000181905550600260008873ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600082815260200190815260200160002060010154600260008973ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160010181905550611caa600760009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16611ca5600a611c976005543461255d90919063ffffffff16565b61259790919063ffffffff16565b6125bd565b611d4b6006600083815260200190815260200160002060008973ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16611d46600a611d386004543461255d90919063ffffffff16565b61259790919063ffffffff16565b6125bd565b600260008873ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160000154600260008973ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160010154889550955095505050509193909250565b6000600260008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600201549050919050565b60008060006003543414611e4a57600080fd5b6000600260008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000016000868152602001908152602001600020600001541415611eae57600080fd5b611f01600760009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16611efc600a611eee6005543461255d90919063ffffffff16565b61259790919063ffffffff16565b6125bd565b611fa26006600086815260200190815260200160002060008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16611f9d600a611f8f6004543461255d90919063ffffffff16565b61259790919063ffffffff16565b6125bd565b600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600085815260200190815260200160002060000154600260008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600086815260200190815260200160002060010154600260008873ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000016000878152602001908152602001600020600201549250925092509250925092565b600060035434146120c457600080fd5b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663cbc7ef096040518163ffffffff1660e01b815260040160206040518083038186803b15801561212e57600080fd5b505afa158015612142573d6000803e3d6000fd5b505050506040513d602081101561215857600080fd5b810190808051906020019092919050505090506000612180824361251e90919063ffffffff16565b90506000600260008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206001016002015490505b818110158061223257506000600260008873ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600083815260200190815260200160002060000154145b156122a357600260008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000016000828152602001908152602001600020600201549050600081141561229e576122a3565b6121cd565b6122f6600760009054906101000a900473ffffffffffffffffffffffffffffffffffffffff166122f1600a6122e36005543461255d90919063ffffffff16565b61259790919063ffffffff16565b6125bd565b6123976006600083815260200190815260200160002060008873ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16612392600a6123846004543461255d90919063ffffffff16565b61259790919063ffffffff16565b6125bd565b600081905060008090505b868210612510576000600260008a73ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000160008581526020019081526020016000206001015490506000600260008b73ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001600086815260200190815260200160002060000154905061249e61248f82612481670de0b6b3a76400008661255d90919063ffffffff16565b61259790919063ffffffff16565b8461253e90919063ffffffff16565b9250600260008b73ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000160008581526020019081526020016000206002015493506000851415612509575050612510565b50506123a2565b809550505050505092915050565b60008282111561252d57600080fd5b600082840390508091505092915050565b60008082840190508381101561255357600080fd5b8091505092915050565b6000808314156125705760009050612591565b600082840290508284828161258157fe5b041461258c57600080fd5b809150505b92915050565b60008082116125a557600080fd5b60008284816125b057fe5b0490508091505092915050565b60006125de8373ffffffffffffffffffffffffffffffffffffffff1661262c565b90508073ffffffffffffffffffffffffffffffffffffffff166108fc839081150290604051600060405180830381858888f19350505050158015612626573d6000803e3d6000fd5b50505050565b600081905091905056fea265627a7a72315820e961774810181d4e3297584398d2c21a73cc0e2e50990591f0944d5bfcde01ce64736f6c634300050c0032";

    protected NestOfferPriceContract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected NestOfferPriceContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<NowTokenPriceEventResponse> getNowTokenPriceEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("nowTokenPrice", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<NowTokenPriceEventResponse> responses = new ArrayList<NowTokenPriceEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            NowTokenPriceEventResponse typedResponse = new NowTokenPriceEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.a = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.b = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.c = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<NowTokenPriceEventResponse> nowTokenPriceEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("nowTokenPrice", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, NowTokenPriceEventResponse>() {
            @Override
            public NowTokenPriceEventResponse call(Log log) {
                EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                NowTokenPriceEventResponse typedResponse = new NowTokenPriceEventResponse();
                typedResponse.log = log;
                typedResponse.a = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.b = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.c = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public static RemoteCall<NestOfferPriceContract> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String map) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(map)));
        return deployRemoteCall(NestOfferPriceContract.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static RemoteCall<NestOfferPriceContract> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String map) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new Address(map)));
        return deployRemoteCall(NestOfferPriceContract.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public RemoteCall<TransactionReceipt> addPrice(BigInteger _ethAmount, BigInteger _tokenAmount, String _tokenAddress) {
        final Function function = new Function(
                "addPrice", 
                Arrays.<Type>asList(new Uint256(_ethAmount),
                new Uint256(_tokenAmount),
                new Address(_tokenAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changeMapping(String map) {
        final Function function = new Function(
                "changeMapping", 
                Arrays.<Type>asList(new Address(map)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changePrice(BigInteger _ethAmount, BigInteger _tokenAmount, String _tokenAddress, BigInteger blockNum) {
        final Function function = new Function(
                "changePrice", 
                Arrays.<Type>asList(new Uint256(_ethAmount),
                new Uint256(_tokenAmount),
                new Address(_tokenAddress),
                new Uint256(blockNum)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changePriceCost(BigInteger amount) {
        final Function function = new Function(
                "changePriceCost", 
                Arrays.<Type>asList(new Uint256(amount)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> changePriceCostProportion(BigInteger user, BigInteger abonus) {
        final Function function = new Function(
                "changePriceCostProportion", 
                Arrays.<Type>asList(new Uint256(user),
                new Uint256(abonus)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> checkLatestBlock(String token) {
        final Function function = new Function("checkLatestBlock", 
                Arrays.<Type>asList(new Address(token)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> checkPriceCost() {
        final Function function = new Function("checkPriceCost", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<Tuple2<BigInteger, BigInteger>> checkPriceCostProportion() {
        final Function function = new Function("checkPriceCostProportion", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteCall<Tuple2<BigInteger, BigInteger>>(
                new Callable<Tuple2<BigInteger, BigInteger>>() {
                    @Override
                    public Tuple2<BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    public RemoteCall<Tuple3<BigInteger, BigInteger, BigInteger>> checkPriceForBlock(String tokenAddress, BigInteger blockNum) {
        final Function function = new Function("checkPriceForBlock", 
                Arrays.<Type>asList(new Address(tokenAddress),
                new Uint256(blockNum)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteCall<Tuple3<BigInteger, BigInteger, BigInteger>>(
                new Callable<Tuple3<BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }

    public RemoteCall<TransactionReceipt> checkPriceForBlockPay(String tokenAddress, BigInteger blockNum, BigInteger weiValue) {
        final Function function = new Function(
                "checkPriceForBlockPay", 
                Arrays.<Type>asList(new Address(tokenAddress),
                new Uint256(blockNum)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<TransactionReceipt> checkPriceHistoricalAverage(String tokenAddress, BigInteger blockNum, BigInteger weiValue) {
        final Function function = new Function(
                "checkPriceHistoricalAverage", 
                Arrays.<Type>asList(new Address(tokenAddress),
                new Uint256(blockNum)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<Tuple2<BigInteger, BigInteger>> checkPriceNow(String tokenAddress) {
        final Function function = new Function("checkPriceNow", 
                Arrays.<Type>asList(new Address(tokenAddress)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteCall<Tuple2<BigInteger, BigInteger>>(
                new Callable<Tuple2<BigInteger, BigInteger>>() {
                    @Override
                    public Tuple2<BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    public RemoteCall<TransactionReceipt> updateAndCheckPriceNow(String _tokenAddress, BigInteger weiValue) {
        final Function function = new Function(
                "updateAndCheckPriceNow", 
                Arrays.<Type>asList(new Address(_tokenAddress)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public static NestOfferPriceContract load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new NestOfferPriceContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static NestOfferPriceContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new NestOfferPriceContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class NowTokenPriceEventResponse {
        public Log log;

        public String a;

        public BigInteger b;

        public BigInteger c;
    }
}
