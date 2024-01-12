package onlydust.com.marketplace.accounting.domain;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

public class CurrencyService {
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

    public void addERC20Support(Blockchain blockchain, ContractAddress tokenAddress) {
        if (erc20Storage.exists(blockchain, tokenAddress))
            throw badRequest("ERC20 token at address %s on %s is already supported".formatted(tokenAddress, blockchain.pretty()));

        final var token = erc20ProviderFactory.get(blockchain)
                .get(tokenAddress)
                .orElseThrow(() -> notFound("Could not find a valid ERC20 contract at address %s on %s".formatted(tokenAddress,
                        blockchain.pretty())));

        erc20Storage.save(token);

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
