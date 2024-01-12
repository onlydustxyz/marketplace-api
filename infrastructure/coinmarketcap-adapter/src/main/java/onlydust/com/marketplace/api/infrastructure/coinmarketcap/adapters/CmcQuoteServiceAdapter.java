package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.ERC20Storage;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteService;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class CmcQuoteServiceAdapter implements QuoteService {
    private final @NonNull CmcClient client;
    private final @NonNull CurrencyStorage currencyStorage;
    private final @NonNull ERC20Storage erc20Storage;
    private final @NonNull Map<Currency.Id, Integer> internalIds = new HashMap<>();

    public CmcQuoteServiceAdapter(final @NonNull CmcClient client, final @NonNull CurrencyStorage currencyStorage, final @NonNull ERC20Storage erc20Storage, final @NonNull Properties properties) {
        this.client = client;
        this.currencyStorage = currencyStorage;
        this.erc20Storage = erc20Storage;
        this.internalIds.putAll(properties.currencyIds);
    }

    @Override
    public Optional<Quote> currentPrice(Currency.Id currencyId, ERC20 token, Currency.Id base) {
        final var baseId = Optional.ofNullable(internalIds.get(base)).orElseThrow(() -> badRequest("Currency %s is not supported as base".formatted(base)));

        final var typeRef = new TypeReference<CmcClient.Response<Map<String, List<Data>>>>() {
        };

        return client.get("/v2/cryptocurrency/quotes/latest?symbol=%s&convert_id=%d".formatted(token.symbol(), baseId), typeRef).flatMap(data -> data.get(token.symbol()).stream().filter(d -> token.address().equals(d.platform.tokenAddress)).findFirst().map(d -> d.quote.get(baseId)).map(q -> new Quote(currencyId, base, q.price)));
    }

    @Override
    public List<Optional<Quote>> currentPrice(List<Currency.Id> currencies, Currency.Id base) {
        refreshInternalIds();

        final var baseId = Optional.ofNullable(internalIds.get(base)).orElseThrow(() -> badRequest("Currency %s is not supported as base".formatted(base)));

        final var currencyIds = currencies.stream().map(this.internalIds::get).filter(Objects::nonNull).map(Object::toString).toArray(String[]::new);

        final var typeRef = new TypeReference<CmcClient.Response<Map<String, Data>>>() {
        };

        final var response = client.get("/v2/cryptocurrency/quotes/latest?id=%s&convert_id=%d".formatted(String.join(",", currencyIds), baseId), typeRef).orElseThrow(() -> internalServerError("Unable to fetch quotes"));

        return currencies.stream().map(c -> Optional.ofNullable(internalIds.get(c)).map(id -> response.get(id.toString())).map(d -> d.quote.get(baseId)).map(q -> new Quote(c, base, q.price))).toList();
    }

    private void refreshInternalIds() {
        final var missingTokens = erc20Storage.all().stream().filter(erc20 -> !internalIds.containsKey(currencyStorage.findByCode(Currency.Code.of(erc20.symbol())).orElseThrow().id())).toList();

        if (missingTokens.isEmpty())
            return;

        final var typeRef = new TypeReference<CmcClient.Response<List<Data>>>() {
        };

        final var path = "/v1/cryptocurrency/map?aux=platform&symbol=%s".formatted(missingTokens.stream().map(ERC20::symbol).collect(Collectors.joining(",")));
        final var response = client.get(path, typeRef).orElseThrow(() -> internalServerError("Unable to fetch currency ids"));

        missingTokens.forEach(erc20 -> {
            final var currency = currencyStorage.findByCode(Currency.Code.of(erc20.symbol())).orElseThrow();
            response.stream().filter(d -> erc20.address().equals(d.platform.tokenAddress)).findFirst().ifPresent(data -> internalIds.put(currency.id(), data.id()));
        });
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Data(Integer id, Platform platform, Map<Integer, QuoteResponse> quote) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record QuoteResponse(BigDecimal price) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Platform(@JsonProperty("token_address") ContractAddress tokenAddress) {
        }
    }

    @lombok.Data
    @AllArgsConstructor
    public static class Properties {
        Map<Currency.Id, Integer> currencyIds;
    }
}
