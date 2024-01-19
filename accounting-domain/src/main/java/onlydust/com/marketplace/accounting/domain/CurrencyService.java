package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class CurrencyService implements CurrencyFacadePort {
    private final @NonNull ERC20ProviderFactory erc20ProviderFactory;
    private final @NonNull CurrencyStorage currencyStorage;
    private final @NonNull CurrencyMetadataService currencyMetadataService;
    private final @NonNull QuoteService quoteService;
    private final @NonNull QuoteStorage quoteStorage;
    private final @NonNull IsoCurrencyService isoCurrencyService;
    private final @NonNull ImageStoragePort imageStoragePort;

    @Override
    public Currency addERC20Support(final @NonNull Blockchain blockchain, final @NonNull ContractAddress tokenAddress) {
        final var token = erc20ProviderFactory.get(blockchain)
                .get(tokenAddress)
                .orElseThrow(
                        () -> notFound("Could not find a valid ERC20 contract at address %s on %s".formatted(tokenAddress, blockchain.pretty())));

        final var currency = currencyStorage.findByCode(Currency.Code.of(token.getSymbol()))
                .map(c -> c.withERC20(token))
                .orElseGet(() -> currencyMetadataService.get(token)
                        .map(metadata -> Currency.of(token).withMetadata(metadata))
                        .orElse(Currency.of(token)));

        saveCurrency(currency);
        return currency;
    }

    @Override
    public Currency addNativeCryptocurrencySupport(final @NonNull Currency.Code code, final @NonNull Integer decimals) {
        if (currencyStorage.exists(code)) {
            throw badRequest("Currency %s already exists".formatted(code));
        }

        final var metadata = currencyMetadataService.get(code)
                .orElseThrow(() -> notFound("Could not find metadata for crypto currency %s".formatted(code)));

        final var currency = Currency.crypto(metadata.name(), code, decimals).withMetadata(metadata);

        saveCurrency(currency);
        return currency;
    }

    @Override
    public Currency addIsoCurrencySupport(final @NonNull Currency.Code code) {
        final var currency = currencyStorage.findByCode(code)
                .or(() -> isoCurrencyService.get(code))
                .orElseThrow(() -> notFound("Could not find ISO currency %s".formatted(code)));

        saveCurrency(currency);
        return currency;
    }

    private void saveCurrency(Currency currency) {
        currencyStorage.save(currency);
        saveQuotes(Set.of(currency));
    }

    @Override
    public void refreshQuotes() {
        final var currencies = currencyStorage.all();
        if (!currencies.isEmpty())
            saveQuotes(currencies);
    }

    @Override
    public Currency updateCurrency(Currency.Id id, String name, String description, URI logoUrl, Integer decimals) {
        var currency = currencyStorage.get(id).orElseThrow(() -> notFound("Currency %s not found".formatted(id)));

        if (name != null)
            currency = currency.withName(name);
        if (description != null)
            currency = currency.withMetadata(new Currency.Metadata(currency.name(), description, currency.logoUri().orElse(null)));
        if (logoUrl != null)
            currency = currency.withMetadata(new Currency.Metadata(currency.name(), currency.description().orElse(null), logoUrl));
        if (decimals != null)
            currency = currency.withDecimals(decimals);

        currencyStorage.save(currency);

        return currency;
    }

    @Override
    public URL uploadLogo(InputStream imageInputStream) {
        return imageStoragePort.storeImage(imageInputStream);
    }

    @Override
    public Collection<Currency> listCurrencies() {
        return currencyStorage.all().stream().sorted(Comparator.comparing(c -> c.code().toString())).toList();
    }

    private void saveQuotes(Set<Currency> currencies) {
        final var bases = new HashSet<>(currencyStorage.all());
        bases.addAll(currencies);
        final var quotes = quoteService.currentPrice(currencies, bases);
        if (!quotes.isEmpty())
            quoteStorage.save(quotes);
    }
}
