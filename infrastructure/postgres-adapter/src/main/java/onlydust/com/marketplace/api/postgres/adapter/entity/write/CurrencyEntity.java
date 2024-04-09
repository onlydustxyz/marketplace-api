package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "currencies", schema = "public")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Accessors(fluent = true)
@TypeDef(name = "currency_type", typeClass = PostgreSQLEnumType.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@ToString
public class CurrencyEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID id;
    @org.hibernate.annotations.Type(type = "currency_type")
    @Enumerated(EnumType.STRING)
    private @NonNull Type type;
    private @NonNull String name;
    private @NonNull String code;
    private String logoUrl;
    private @NonNull Integer decimals;
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "currency_id", insertable = false, updatable = false)
    LatestUsdQuoteEntity latestUsdQuote;

    @OneToMany(mappedBy = "currencyId", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ERC20Entity> erc20;

    public static CurrencyEntity of(Currency currency) {
        return CurrencyEntity.builder()
                .id(currency.id().value())
                .type(Type.of(currency.type()))
                .name(currency.name())
                .code(currency.code().toString())
                .logoUrl(currency.logoUri().map(Objects::toString).orElse(null))
                .decimals(currency.decimals())
                .description(currency.description().orElse(null))
                .erc20(currency.erc20().stream().map(erc20 -> ERC20Entity.of(currency.id(), erc20)).collect(Collectors.toUnmodifiableSet()))
                .build();
    }

    public Currency toDomain() {
        return Currency.builder()
                .id(Currency.Id.of(id))
                .type(type.toDomain())
                .name(name)
                .code(Currency.Code.of(code))
                .metadata(new Currency.Metadata(name, description, logoUrl == null ? null : URI.create(logoUrl)))
                .decimals(decimals)
                .erc20(erc20.stream().map(ERC20Entity::toDomain).collect(Collectors.toUnmodifiableSet()))
                .latestUsdQuote(latestUsdQuote == null ? null : latestUsdQuote.getPrice())
                .build();
    }

    public CurrencyView toView() {
        return CurrencyView.builder()
                .id(CurrencyView.Id.of(id))
                .name(name)
                .code(code)
                .logoUrl(logoUrl == null ? null : URI.create(logoUrl))
                .decimals(decimals)
                .build();
    }


    public enum Type {
        FIAT, CRYPTO;

        public static Type of(final @NonNull Currency.Type type) {
            return switch (type) {
                case FIAT -> FIAT;
                case CRYPTO -> CRYPTO;
            };
        }

        public Currency.Type toDomain() {
            return switch (this) {
                case FIAT -> Currency.Type.FIAT;
                case CRYPTO -> Currency.Type.CRYPTO;
            };
        }
    }
}
