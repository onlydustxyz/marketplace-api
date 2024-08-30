package onlydust.com.marketplace.api.infura.adapters;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.api.infura.Web3Client;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;
import org.web3j.tx.exceptions.ContractCallException;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

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
            throw internalServerError("Unable to fetch ERC20 name at address %s".formatted(address), e);
        }
    }
}
