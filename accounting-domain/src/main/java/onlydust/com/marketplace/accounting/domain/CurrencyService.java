package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyMetadataService;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteService;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteStorage;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class CurrencyService {
    private final ERC20ProviderFactory erc20ProviderFactory;
    private final CurrencyStorage currencyStorage;
    private final CurrencyMetadataService currencyMetadataService;
    private final QuoteService quoteService;
    private final QuoteStorage quoteStorage;
    private final Currency usd;

    public CurrencyService(ERC20ProviderFactory erc20ProviderFactory, CurrencyStorage currencyStorage, CurrencyMetadataService currencyMetadataService,
                           QuoteService quoteService, QuoteStorage quoteStorage) {
        this.erc20ProviderFactory = erc20ProviderFactory;
        this.currencyStorage = currencyStorage;
        this.currencyMetadataService = currencyMetadataService;
        this.quoteService = quoteService;
        this.quoteStorage = quoteStorage;
        this.usd = currencyStorage.findByCode(Currency.Code.USD)
                .orElseThrow(() -> internalServerError("USD currency is not available"));
    }

    public void addERC20Support(Blockchain blockchain, ContractAddress tokenAddress) {
        final var token = erc20ProviderFactory.get(blockchain)
                .get(tokenAddress)
                .orElseThrow(() -> OnlyDustException.notFound("Could not find a valid ERC20 contract at address %s on %s".formatted(tokenAddress,
                        blockchain.pretty())));

        final var currency = Currency.of(token);
        if (!currencyStorage.exists(currency.code())) {
            final var metadata = currencyMetadataService.get(token);
            currencyStorage.save(metadata.map(currency::withMetadata).orElse(currency));

            quoteService.currentPrice(currency.id(), token, usd.id())
                    .ifPresent(quoteStorage::save);
        }
    }

    public void refreshQuotes() {
        final var currencies = currencyStorage.all().stream().map(Currency::id).toList();
        final var quotes = quoteService.currentPrice(currencies, usd.id()).stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(Quote[]::new);

        quoteStorage.save(quotes);
    }
}
