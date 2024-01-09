package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Provider;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteService;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

@AllArgsConstructor
public class CurrencyService {
    private final ERC20ProviderFactory erc20ProviderFactory;
    private final CurrencyStorage currencyStorage;
    private final QuoteService quoteService;
    private final QuoteStorage quoteStorage;

    public void addERC20Support(Blockchain blockchain, ContractAddress tokenAddress) {
        final var token = erc20ProviderFactory.get(blockchain)
                .get(tokenAddress)
                .orElseThrow(() -> OnlyDustException.notFound("Could not find a valid ERC20 contract at address %s on %s".formatted(tokenAddress, blockchain.pretty())));

        final var currency = Currency.of(token);
        if(!currencyStorage.exists(currency.code())) {
            currencyStorage.save(currency);

            quoteService.currentPrice(token, Currency.Code.USD)
                    .ifPresent(price -> quoteStorage.save(new Quote(currency.id(), Currency.Code.USD, price)));
        }
    }
}
