package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

@AllArgsConstructor
public class CurrencyService {
    private final ERC20Provider erc20Provider;
    private final CurrencyStorage currencyStorage;

    public void addERC20Support(Blockchain blockchain, ContractAddress tokenAddress) {
        if (!blockchain.isEvmCompatible()) {
            throw OnlyDustException.badRequest("%s is not EVM compatible".formatted(blockchain.pretty()));
        }

        final var token = erc20Provider.get(tokenAddress);
        currencyStorage.save(Currency.of(token));
    }
}
