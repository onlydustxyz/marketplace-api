package onlydust.com.marketplace.accounting.domain;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.util.List;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

public class CurrencyService implements onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort {
    private final @NonNull ERC20ProviderFactory erc20ProviderFactory;
    private final @NonNull ERC20Storage erc20Storage;
    private final @NonNull CurrencyStorage currencyStorage;
    private final @NonNull CurrencyMetadataService currencyMetadataService;
    private final @NonNull QuoteService quoteService;
    private final @NonNull QuoteStorage quoteStorage;
    private final @NonNull Currency usd;

    public CurrencyService(final @NonNull ERC20ProviderFactory erc20ProviderFactory,
                           final @NonNull ERC20Storage erc20Storage,
                           final @NonNull CurrencyStorage currencyStorage,
                           final @NonNull CurrencyMetadataService currencyMetadataService,
                           final @NonNull QuoteService quoteService,
                           final @NonNull QuoteStorage quoteStorage) {
        this.erc20ProviderFactory = erc20ProviderFactory;
        this.erc20Storage = erc20Storage;
        this.currencyStorage = currencyStorage;
        this.currencyMetadataService = currencyMetadataService;
        this.quoteService = quoteService;
        this.quoteStorage = quoteStorage;
        this.usd = currencyStorage.findByCode(Currency.Code.USD)
                .orElseThrow(() -> internalServerError("USD currency is not available"));
    }

    @Override
    public Currency addERC20Support(final @NonNull Blockchain blockchain, final @NonNull ContractAddress tokenAddress) {
        if (erc20Storage.exists(blockchain, tokenAddress))
            throw badRequest("ERC20 token at address %s on %s is already supported".formatted(tokenAddress, blockchain.pretty()));

        final var token = erc20ProviderFactory.get(blockchain)
                .get(tokenAddress)
                .orElseThrow(
                        () -> notFound("Could not find a valid ERC20 contract at address %s on %s".formatted(tokenAddress, blockchain.pretty())));

        erc20Storage.save(token);

        return currencyStorage.findByCode(Currency.Code.of(token.symbol()))
                .map(c -> c.withERC20(token))
                .orElseGet(() -> createCurrency(token));
    }

    private Currency createCurrency(ERC20 token) {
        final var currency = currencyMetadataService.get(token)
                .map(metadata -> Currency.of(token).withMetadata(metadata))
                .orElse(Currency.of(token));

        currencyStorage.save(currency);

        saveUsdQuotes(List.of(currency));
        return currency;
    }

    @Override
    public void refreshQuotes() {
        saveUsdQuotes(currencyStorage.all());
    }

    private void saveUsdQuotes(List<Currency> currencies) {
        quoteService.currentPrice(currencies, usd).forEach(quoteStorage::save);
    }
}
