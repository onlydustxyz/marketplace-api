package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteService;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ContractAddress;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@AllArgsConstructor
public class CmcQuoteServiceAdapter implements QuoteService {
    private final CmcClient client;
    private final Properties properties;

    @Override
    public Optional<Quote> currentPrice(Currency.Id currencyId, ERC20 token, Currency.Code base) {
        final var baseId = Optional.ofNullable(properties.currencyIds.get(base))
                .orElseThrow(() -> badRequest("Currency %s is not supported as base".formatted(base)));

        final var typeRef = new TypeReference<CmcClient.Response<Map<String, List<Data>>>>() {
        };

        return client.get("/v2/cryptocurrency/quotes/latest?symbol=%s&convert_id=%d".formatted(token.symbol(), baseId), typeRef)
                .flatMap(data -> data.get(token.symbol()).stream()
                        .filter(d -> token.address().equals(d.platform.tokenAddress))
                        .findFirst()
                        .map(d -> d.quote.get(baseId))
                        .map(q -> new Quote(currencyId, base, q.price))
                );
    }

    @Override
    public List<Optional<Quote>> currentPrice(List<Currency.Id> currencies, Currency.Code base) {
        return List.of();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Data(Platform platform, Map<Integer, QuoteResponse> quote) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record QuoteResponse(BigDecimal price) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Platform(@JsonProperty("token_address") ContractAddress tokenAddress) {
        }
    }

    public record Properties(Map<Currency.Code, Integer> currencyIds) {
    }
}
