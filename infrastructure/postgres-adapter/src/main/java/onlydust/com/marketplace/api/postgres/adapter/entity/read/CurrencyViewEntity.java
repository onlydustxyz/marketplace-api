package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.ERC20Entity;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "currencies", schema = "public")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@ToString
@Immutable
public class CurrencyViewEntity {
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "currency_id", insertable = false, updatable = false)
    LatestUsdQuoteViewEntity latestUsdQuote;

    @OneToMany(mappedBy = "currencyId", fetch = FetchType.EAGER)
    private Set<ERC20Entity> erc20;

    public Currency toDomain() {
        return Currency.builder()
                .id(Currency.Id.of(id))
                .type(type)
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
                .latestUsdQuote(latestUsdQuote == null ? null : latestUsdQuote.getPrice())
                .build();
    }
}
