package onlydust.com.marketplace.api.infrastructure.aptosrpc;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.netty.handler.codec.http.HttpMethod;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.infrastructure.HttpClient;

import java.math.BigInteger;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class RpcClient extends HttpClient {
    private final Properties properties;

    public RpcClient(Properties properties) {
        this.properties = properties;
    }

    @Override
    protected HttpRequest.Builder builder() {
        return HttpRequest.newBuilder();
    }

    @Override
    protected URI uri(String path) {
        return URI.create(properties.baseUri + path);
    }

    public Optional<AccountResponse> getAccount(String address) {
        return send("/accounts/%s".formatted(address), HttpMethod.GET, null, AccountResponse.class);
    }

    public Optional<TransactionResponse> getTransactionByHash(String hash) {
        return send("/transactions/by_hash/%s".formatted(hash), HttpMethod.GET, null, TransactionResponse.class);
    }

    public Optional<TransactionResourceResponse> getAccountResource(String hash, String resource) {
        return send("/accounts/%s/resource/%s".formatted(hash, URLEncoder.encode(resource, StandardCharsets.UTF_8)), HttpMethod.GET, null,
                TransactionResourceResponse.class);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Properties {
        String baseUri;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record AccountResponse(String sequenceNumber, String authenticationKey) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransactionResponse(Long version, String hash, Long timestamp, Boolean success, Payload payload, List<Event> events) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Value
        @Accessors(fluent = true)
        @NoArgsConstructor(force = true)
        @AllArgsConstructor
        @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Event {
            Guid guid;
            String sequenceNumber;
            @Getter(AccessLevel.NONE)
            @NonNull
            String type;
            Data data;

            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
            public record Guid(String creationNumber, String accountAddress) {
            }

            public Type type() {
                return switch (type) {
                    case "0x1::coin::DepositEvent" -> Type.DEPOSIT;
                    case "0x1::coin::WithdrawEvent" -> Type.WITHDRAWAL;
                    case "0x1::transaction_fee::FeeStatement" -> Type.FEE_STATEMENT;
                    default -> {
                        LOGGER.warn("Unknown event type: {}", type);
                        yield null;
                    }
                };
            }

            public enum Type {
                DEPOSIT, WITHDRAWAL, FEE_STATEMENT
            }

            @AllArgsConstructor
            public static class Data {
                final Map<String, Object> properties = new HashMap<>();

                public Data(Map<String, Object> properties) {
                    this.properties.putAll(properties);
                }

                @JsonAnySetter
                protected void add(String property, Object value) {
                    properties.put(property, value);
                }

                @JsonAnyGetter
                protected Map<String, Object> properties() {
                    return properties;
                }

                public Deposit asDeposit() {
                    return new Deposit(properties);
                }

                public Withdrawal asWithdrawal() {
                    return new Withdrawal(properties);
                }

                @AllArgsConstructor
                public static class Deposit extends Data {
                    public Deposit(Map<String, Object> properties) {
                        super(properties);
                    }

                    public BigInteger amount() {
                        return new BigInteger(properties().get("amount").toString());
                    }
                }

                public static class Withdrawal extends Data {
                    public Withdrawal(Map<String, Object> properties) {
                        super(properties);
                    }

                    public BigInteger amount() {
                        return new BigInteger(properties().get("amount").toString());
                    }
                }
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public record Payload(String function, List<String> typeArguments, List<String> arguments, String type) {
            public boolean isTransfer() {
                return "0x1::aptos_account::transfer_coins".equals(function);
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransactionResourceResponse(String type, Data data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Data(java.lang.Integer decimals, String name, String symbol, Supply supply) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Supply(List<Data> vec) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Data(Integer integer) {
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Integer(List<Data> vec) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Data(BigInteger limit, BigInteger value) {
            }
        }
    }
}
