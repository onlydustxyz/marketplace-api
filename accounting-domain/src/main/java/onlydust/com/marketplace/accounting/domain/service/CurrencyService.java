package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.ERC20ProviderFactory;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

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
    public Currency addERC20Support(final @NonNull Blockchain blockchain, final @NonNull Hash tokenAddress) {
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
    public Currency addIsoCurrencySupport(final @NonNull Currency.Code code, final String description, final URI logoUri) {
        var currency = currencyStorage.findByCode(code)
                .or(() -> isoCurrencyService.get(code))
                .orElseThrow(() -> notFound("Could not find ISO currency %s".formatted(code)));

        final URI savedLogoUrl;
        try {
            savedLogoUrl = logoUri == null ? null : imageStoragePort.storeImage(logoUri).toURI();
        } catch (URISyntaxException e) {
            throw badRequest("Invalid logo URI: %s".formatted(logoUri));
        }

        currency = currency.withMetadata(new Currency.Metadata(
                currencyMetadataService.fiatId(code),
                currency.name(),
                Optional.ofNullable(description).or(currency::description).orElse(null),
                Optional.ofNullable(savedLogoUrl).or(currency::logoUri).orElse(null)
        ));

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
    public Currency updateCurrency(Currency.Id id,
                                   String name,
                                   String description,
                                   URI logoUrl,
                                   Integer decimals,
                                   List<String> countryRestrictions,
                                   Integer cmcId) {
        var currency = currencyStorage.get(id).orElseThrow(() -> notFound("Currency %s not found".formatted(id)));

        if (name != null)
            currency = currency.withName(name);
        if (description != null)
            currency = currency.withMetadata(new Currency.Metadata(currency.cmcId(), currency.name(), description, currency.logoUri().orElse(null)));
        if (logoUrl != null) {
            final var savedLogoUrl = imageStoragePort.storeImage(logoUrl);
            currency = currency.withMetadata(new Currency.Metadata(currency.cmcId(), currency.name(), currency.description().orElse(null),
                    URI.create(savedLogoUrl.toString())));
        }
        if (decimals != null)
            currency = currency.withDecimals(decimals);
        if (countryRestrictions != null)
            currency = currency.withCountryRestrictions(countryRestrictions);
        if (cmcId != null)
            currency = currency.withMetadata(new Currency.Metadata(cmcId, currency.name(), currency.description().orElse(null),
                    currency.logoUri().orElse(null)));

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

    @Override
    public BigDecimal latestQuote(Currency.Code fromCode, Currency.Code toCode) {
        final var from = currencyStorage.findByCode(fromCode).orElseThrow(() -> notFound("Currency %s not found".formatted(fromCode)));
        final var to = currencyStorage.findByCode(toCode).orElseThrow(() -> notFound("Currency %s not found".formatted(toCode)));

        return quoteStorage.latest(from.id(), to.id())
                .stream()
                .findFirst()
                .map(Quote::price)
                .orElseThrow(() -> notFound("Could not find quote from %s to %s".formatted(from, to)));
    }

    @Override
    public Currency get(Currency.Id id) {
        return currencyStorage.get(id).orElseThrow(() -> notFound("Currency %s not found".formatted(id)));
    }

    private void saveQuotes(Set<Currency> currencies) {
        final var usd = currencyStorage.findByCode(Currency.Code.USD)
                .orElseThrow(() -> internalServerError("USD currency not found"));

        final var quotes = quoteService.currentPrice(currencies, Set.of(usd));
        if (!quotes.isEmpty())
            quoteStorage.save(quotes);
    }
}
