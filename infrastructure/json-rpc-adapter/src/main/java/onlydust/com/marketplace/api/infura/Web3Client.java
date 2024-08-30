package onlydust.com.marketplace.api.infura;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class Web3Client {
    protected final Web3j web3j;
    protected final DefaultGasProvider gasPriceProvider;
    protected final Credentials credentials;
    private final Properties properties;

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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Properties {
        String baseUri;
        String privateKey;
        Blockchain blockchain;
    }

    protected static class ERC20Contract extends org.web3j.contracts.eip20.generated.ERC20 {
        protected ERC20Contract(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
            super(contractAddress, web3j, credentials, contractGasProvider);
        }

        public static ERC20Contract load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
            return new ERC20Contract(contractAddress, web3j, credentials, contractGasProvider);
        }

        public CompletableFuture<String> nameWithBinaryFallback() {
            return super.name().sendAsync().thenCompose(r -> r.isEmpty() ? binaryCallAsync("name") : completedFuture(r));
        }

        public CompletableFuture<String> symbolWithBinaryFallback() {
            return super.symbol().sendAsync().thenCompose(r -> r.isEmpty() ? binaryCallAsync("symbol") : completedFuture(r));
        }

        private CompletableFuture<String> binaryCallAsync(final @NonNull String method) {
            final var function = new Function(method, List.of(), List.of(new TypeReference<Uint256>() {
            }));

            return executeRemoteCallSingleValueReturn(function, BigInteger.class).sendAsync()
                    .thenApply(ERC20Contract::dec2ascii);
        }

        private static String dec2ascii(BigInteger dec) {
            return sanitize(hex2ascii(dec.toString(16)));
        }

        private static String hex2ascii(String hex) {
            return new String(HexFormat.of().parseHex(hex));
        }

        private static String sanitize(String value) {
            return value.replace("\0", "");
        }
    }
}
