package onlydust.com.marketplace.api.infura;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.infura.contracts.ERC20;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigInteger;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class Web3Client {
    protected final Web3j web3j;
    protected final DefaultGasProvider gasPriceProvider;
    protected final Credentials credentials;
    private final Properties properties;

    private final static BigInteger ERC20_INTERFACE_ID = BigInteger.valueOf(0x80ac58cd);

    public Web3Client(Properties properties) {
        this.web3j = switch (properties.blockchain) {
            case ETHEREUM, OPTIMISM -> Web3j.build(new HttpService(properties.baseUri));
            default -> throw new IllegalArgumentException("Unsupported blockchain: %s".formatted(properties.blockchain));
        };
        this.gasPriceProvider = new DefaultGasProvider();
        this.credentials = Credentials.create(properties.privateKey);
        this.properties = properties;
    }

    public Blockchain blockchain() {
        return properties.blockchain;
    }

    protected EthTransaction getTransactionByHash(String hash) {
        try {
            return web3j.ethGetTransactionByHash(hash).send();
        } catch (IOException e) {
            throw internalServerError("Unable to fetch transaction by hash %s".formatted(hash), e);
        }
    }

    protected EthGetTransactionReceipt getTransactionReceipt(String hash) {
        try {
            return web3j.ethGetTransactionReceipt(hash).send();
        } catch (IOException e) {
            throw internalServerError("Unable to fetch transaction receipt by hash %s".formatted(hash), e);
        }
    }

    protected EthBlock getBlockByHash(String hash, boolean fullTransactionObjects) {
        try {
            return web3j.ethGetBlockByHash(hash, fullTransactionObjects).send();
        } catch (IOException e) {
            throw internalServerError("Unable to fetch block by hash %s".formatted(hash), e);
        }
    }

    protected boolean isERC20(String tokenAddress) {
        try {
            final var erc20 = ERC20.load(tokenAddress, web3j, credentials, gasPriceProvider);
            return erc20.isValid();
        } catch (Exception e) {
            throw internalServerError("Unable to check ERC20 contract validity", e);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Properties {
        String baseUri;
        String privateKey;
        Blockchain blockchain;
    }
}
