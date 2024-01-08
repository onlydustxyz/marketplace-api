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
    private final ERC20Provider ethereumERC20Provider;
    private final ERC20Provider optimismERC20Provider;
    private final CurrencyStorage currencyStorage;

    public void addERC20Support(Blockchain blockchain, ContractAddress tokenAddress) {
        final var token = (switch (blockchain) {
            case ETHEREUM -> ethereumERC20Provider.get(tokenAddress);
            case OPTIMISM -> optimismERC20Provider.get(tokenAddress);
            default -> throw OnlyDustException.badRequest("ERC20 tokens on %s are not supported".formatted(blockchain.pretty()));
        }).orElseThrow(() -> OnlyDustException.notFound("Could not find a valid ERC20 contract at address %s on %s".formatted(tokenAddress, blockchain.pretty())));

        currencyStorage.save(Currency.of(token));
    }
}
