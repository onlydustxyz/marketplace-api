package onlydust.com.marketplace.api.infura.adapters;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.api.infura.Web3Client;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.exceptions.ContractCallException;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class EthWeb3ERC20ProviderAdapter extends Web3Client implements ERC20Provider {
    public EthWeb3ERC20ProviderAdapter(final Properties properties) {
        super(properties);
    }

    @Override
    public Optional<ERC20> get(@NonNull final Hash address) {
        try {
            final var contract = ERC20Contract.load(address.toString(), web3j, credentials, gasPriceProvider);
            final var name = contract.nameWithBinaryFallback();
            final var symbol = contract.symbolWithBinaryFallback();
            final var decimals = contract.decimals().sendAsync();
            final var totalSupply = contract.totalSupply().sendAsync();

            return Optional.of(new ERC20(blockchain(), address, name.get(), symbol.get(), decimals.get().intValue(), totalSupply.get()));
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof ContractCallException)
                return Optional.empty();
            throw OnlyDustException.internalServerError("Unable to fetch ERC20 name at address %s".formatted(address), e);
        }
    }

    static class ERC20Contract extends org.web3j.contracts.eip20.generated.ERC20 {
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
