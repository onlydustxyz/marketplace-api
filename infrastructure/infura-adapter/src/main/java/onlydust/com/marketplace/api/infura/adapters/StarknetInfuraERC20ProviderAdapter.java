package onlydust.com.marketplace.api.infura.adapters;

import com.swmansion.starknet.data.types.Call;
import com.swmansion.starknet.data.types.Felt;
import com.swmansion.starknet.data.types.Uint256;
import com.swmansion.starknet.provider.Provider;
import com.swmansion.starknet.provider.rpc.JsonRpcProvider;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.api.infura.InfuraClient;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class StarknetInfuraERC20ProviderAdapter implements ERC20Provider {
    Provider provider;
    Blockchain blockchain;

    public StarknetInfuraERC20ProviderAdapter(final InfuraClient.Properties properties) {
        provider = new JsonRpcProvider("%s/%s".formatted(properties.getBaseUri(), properties.getApiKey()));
        blockchain = properties.getBlockchain();
        if (blockchain != Blockchain.STARKNET)
            throw OnlyDustException.internalServerError("Invalid blockchain %s".formatted(blockchain));
    }

    @Override
    public Optional<ERC20> get(@NonNull final Hash address) {
        try {
            final var contract = ERC20Contract.load(provider, address);

            final var name = contract.name();
            final var symbol = contract.symbol();
            final var decimals = contract.decimals();
            final var totalSupply = contract.totalSupply();

            return Optional.of(new ERC20(blockchain, address, name.get(), symbol.get(), decimals.get().intValue(), totalSupply.get()));
        } catch (ExecutionException | InterruptedException e) {
            throw OnlyDustException.internalServerError("Unable to fetch ERC20 name at address %s".formatted(address), e);
        }
    }

    static class ERC20Contract {
        private final Provider provider;
        private final Felt contractAddress;

        private ERC20Contract(Provider provider, Hash contractAddress) {
            this.provider = provider;
            this.contractAddress = Felt.fromHex(contractAddress.toString());
        }

        public static ERC20Contract load(Provider provider, Hash contractAddress) {
            return new ERC20Contract(provider, contractAddress);
        }

        public CompletableFuture<String> name() {
            return provider.callContract(new Call(contractAddress, "name", List.of())).sendAsync().thenApply(r -> r.get(0).toShortString());
        }

        public CompletableFuture<String> symbol() {
            return provider.callContract(new Call(contractAddress, "symbol", List.of())).sendAsync().thenApply(r -> r.get(0).toShortString());
        }

        public CompletableFuture<BigInteger> decimals() {
            return provider.callContract(new Call(contractAddress, "decimals", List.of())).sendAsync().thenApply(r -> new BigInteger(r.get(0).decString()));
        }

        public CompletableFuture<BigInteger> totalSupply() {
            return provider.callContract(new Call(contractAddress, "total_supply", List.of())).sendAsync().thenApply(r -> new Uint256(r.get(0), r.get(1)).getValue());
        }
    }
}
