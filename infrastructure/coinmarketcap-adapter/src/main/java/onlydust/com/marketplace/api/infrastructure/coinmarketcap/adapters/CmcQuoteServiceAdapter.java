package onlydust.com.marketplace.api.infrastructure.coinmarketcap.adapters;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.handler.codec.http.HttpMethod;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.ERC20;
import onlydust.com.marketplace.accounting.domain.model.Quote;
import onlydust.com.marketplace.accounting.domain.port.out.QuoteService;
import onlydust.com.marketplace.api.infrastructure.coinmarketcap.CmcClient;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
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
    public Optional<Quote> currentPrice(ERC20 token, Currency.Code base) {
        final var baseId = Optional.ofNullable(properties.currencyIds.get(base))
                .orElseThrow(() -> badRequest("Currency %s is not supported as base".formatted(base)));

        return client.send("/v2/cryptocurrency/quotes/latest?symbol=%s&convert_id=%d".formatted(token.symbol(), baseId),
                        HttpMethod.GET, null, Response.class)
                .map(this::decode)
                .flatMap(data -> data.get(token.symbol()).stream()
                        .filter(d -> token.address().equals(d.platform.tokenAddress))
                        .findFirst()
                        .map(d -> d.quote.get(baseId))
                        .map(q -> new Quote(Currency.Id.random(), base, q.price))
                );
    }

    @Override
    public List<Optional<Quote>> currentPrice(List<Currency.Id> code, Currency.Code base) {
        return List.of();
    }

    private Map<String, List<Response.Data>> decode(Response response) {
        if (response.status.errorCode > 0)
            throw OnlyDustException.internalServerError("Unable to fetch quotes [%d] %s".formatted(response.status.errorCode, response.status.errorMessage));

        return response.data;
    }

    private record Response(Status status, Map<String, List<Data>> data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Status(@JsonProperty("error_code") Integer errorCode, @JsonProperty("error_message") String errorMessage) {
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
    }

    public record Properties(Map<Currency.Code, Integer> currencyIds) {
    }
}
