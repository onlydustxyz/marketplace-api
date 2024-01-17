package onlydust.com.marketplace.api.infura.adapters;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.api.infura.InfuraClient;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;
import org.web3j.tx.exceptions.ContractCallException;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class InfuraERC20ProviderAdapter extends InfuraClient implements ERC20Provider {
    public InfuraERC20ProviderAdapter(final Properties properties) {
        super(properties);
    }

    @Override
    public Optional<ERC20> get(@NonNull final ContractAddress address) {
        final var contract = org.web3j.contracts.eip20.generated.ERC20.load(address.toString(), web3j, credentials, gasPriceProvider);
        final var name = contract.name().sendAsync();
        final var symbol = contract.symbol().sendAsync();
        final var decimals = contract.decimals().sendAsync();
        final var totalSupply = contract.totalSupply().sendAsync();

        try {
            return Optional.of(new ERC20(blockchain(), address, name.get(), symbol.get(), decimals.get().intValue(), totalSupply.get()));
        } catch (ExecutionException | InterruptedException e) {
            if (e.getCause() instanceof ContractCallException)
                return Optional.empty();
            throw OnlyDustException.internalServerError("Unable to fetch ERC20 name at address %s".formatted(address), e);
        }
    }
}
