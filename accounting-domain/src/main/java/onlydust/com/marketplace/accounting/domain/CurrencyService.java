package onlydust.com.marketplace.accounting.domain;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.util.List;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

@AllArgsConstructor
public class CurrencyService implements CurrencyFacadePort {
    private final @NonNull ERC20ProviderFactory erc20ProviderFactory;
    private final @NonNull ERC20Storage erc20Storage;
    private final @NonNull CurrencyStorage currencyStorage;
    private final @NonNull CurrencyMetadataService currencyMetadataService;
    private final @NonNull QuoteService quoteService;
    private final @NonNull QuoteStorage quoteStorage;

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

    @Override
    public Currency addNativeCryptocurrencySupport(Currency.Code code, Integer decimals) {
        return currencyStorage.findByCode(code)
                .orElseGet(() -> createCurrency(code, decimals));
    }

    private Currency createCurrency(ERC20 token) {
        final var currency = currencyMetadataService.get(token)
                .map(metadata -> Currency.of(token).withMetadata(metadata))
                .orElse(Currency.of(token));

        currencyStorage.save(currency);

        saveUsdQuotes(List.of(currency));
        return currency;
    }

    private Currency createCurrency(Currency.Code code, Integer decimals) {
        final var currency = currencyMetadataService.get(code)
                .map(metadata -> Currency.crypto(metadata.name(), code, decimals).withMetadata(metadata))
                .orElseThrow(() -> notFound("Could not find metadata for crypto currency %s".formatted(code)));

        currencyStorage.save(currency);

        saveUsdQuotes(List.of(currency));
        return currency;
    }

    @Override
    public void refreshQuotes() {
        saveUsdQuotes(currencyStorage.all());
    }

    private void saveUsdQuotes(List<Currency> currencies) {
        final var usd = currencyStorage.findByCode(Currency.Code.USD)
                .orElseThrow(() -> internalServerError("USD currency is not available"));
        quoteService.currentPrice(currencies, usd).forEach(quoteStorage::save);
    }
}
