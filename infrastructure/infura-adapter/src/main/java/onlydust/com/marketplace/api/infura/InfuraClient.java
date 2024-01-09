package onlydust.com.marketplace.api.infura;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

public class InfuraClient {
    protected final Web3j web3j;
    protected final DefaultGasProvider gasPriceProvider;
    protected final Credentials credentials;

    public InfuraClient(Properties properties) {
        this.web3j = Web3j.build(new HttpService("%s/%s".formatted(properties.baseUri, properties.apiKey)));
        this.gasPriceProvider = new DefaultGasProvider();
        this.credentials = Credentials.create(properties.privateKey);
    }

    @Data
    @AllArgsConstructor
    public static class Properties {
        String baseUri;
        String apiKey;
        String privateKey;
    }
}
