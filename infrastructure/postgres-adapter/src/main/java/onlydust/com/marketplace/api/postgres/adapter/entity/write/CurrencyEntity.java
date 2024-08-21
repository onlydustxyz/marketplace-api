package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;

@Entity
@Table(name = "currencies", schema = "public")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CurrencyEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID id;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "currency_type")
    private @NonNull Currency.Type type;
    private @NonNull String name;
    private @NonNull String code;
    private String logoUrl;
    private @NonNull Integer decimals;
    private String description;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @NonNull
    String[] countryRestrictions;
    int cmcId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "currency_id", insertable = false, updatable = false)
    LatestUsdQuoteEntity latestUsdQuote;

    @OneToMany(mappedBy = "currencyId", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ERC20Entity> erc20;

    public static CurrencyEntity of(Currency currency) {
        return CurrencyEntity.builder()
                .id(currency.id().value())
                .type(currency.type())
                .name(currency.name())
                .code(currency.code().toString())
                .logoUrl(currency.logoUri().map(Objects::toString).orElse(null))
                .decimals(currency.decimals())
                .description(currency.description().orElse(null))
                .erc20(currency.erc20().stream().map(erc20 -> ERC20Entity.of(currency.id(), erc20)).collect(toUnmodifiableSet()))
                .countryRestrictions(currency.countryRestrictions().stream().map(Country::iso3Code).toArray(String[]::new))
                .cmcId(currency.cmcId())
                .build();
    }

    public Currency toDomain() {
        return Currency.builder()
                .id(Currency.Id.of(id))
                .type(type)
                .name(name)
                .code(Currency.Code.of(code))
                .metadata(new Currency.Metadata(cmcId, name, description, logoUrl == null ? null : URI.create(logoUrl)))
                .decimals(decimals)
                .erc20(erc20.stream().map(ERC20Entity::toDomain).collect(toUnmodifiableSet()))
                .latestUsdQuote(latestUsdQuote == null ? null : latestUsdQuote.getPrice())
                .countryRestrictions(stream(countryRestrictions).map(Country::fromIso3).collect(toSet()))
                .build();
    }

    public CurrencyView toView() {
        return CurrencyView.builder()
                .id(CurrencyView.Id.of(id))
                .name(name)
                .code(code)
                .logoUrl(logoUrl == null ? null : URI.create(logoUrl))
                .decimals(decimals)
                .latestUsdQuote(latestUsdQuote == null ? null : latestUsdQuote.getPrice())
                .build();
    }

}
