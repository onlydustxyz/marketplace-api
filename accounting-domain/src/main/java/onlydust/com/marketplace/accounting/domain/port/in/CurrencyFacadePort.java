package onlydust.com.marketplace.accounting.domain.port.in;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Hash;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;

public interface CurrencyFacadePort {
    Currency addERC20Support(final @NonNull Blockchain blockchain, final @NonNull Hash tokenAddress);

    Currency addNativeCryptocurrencySupport(Currency.Code code, Integer decimals);

    Currency addIsoCurrencySupport(final @NonNull Currency.Code code, final String description, final URI logoUrl);

    void refreshQuotes();

    Currency updateCurrency(Currency.Id id, String name, String description, URI logoUrl, Integer decimals, List<String> countryRestrictions, Integer cmcId);

    URL uploadLogo(InputStream imageInputStream);

    Collection<Currency> listCurrencies();

    BigDecimal latestQuote(Currency.Code usd, Currency.Code eur);

    Currency get(Currency.Id id);
}
