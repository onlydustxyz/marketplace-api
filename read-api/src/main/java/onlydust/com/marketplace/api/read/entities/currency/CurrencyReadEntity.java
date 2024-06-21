package onlydust.com.marketplace.api.read.entities.currency;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.api.contract.model.ShortCurrencyResponse;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.net.URI;
import java.util.UUID;

@Entity
@Table(name = "currencies", schema = "public")
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@ToString
@Immutable
public class CurrencyReadEntity {
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
    @JoinColumn(name = "id", referencedColumnName = "currencyId", insertable = false, updatable = false)
    LatestUsdQuoteReadEntity latestUsdQuote;

    public ShortCurrencyResponse toShortResponse() {
        return new ShortCurrencyResponse()
                .id(id)
                .name(name)
                .code(code)
                .logoUrl(logoUrl == null ? null : URI.create(logoUrl))
                .decimals(decimals);
    }

    public onlydust.com.backoffice.api.contract.model.ShortCurrencyResponse toBoShortResponse() {
        return new onlydust.com.backoffice.api.contract.model.ShortCurrencyResponse()
                .id(id)
                .name(name)
                .code(code)
                .logoUrl(logoUrl == null ? null : URI.create(logoUrl))
                .decimals(decimals);
    }
}
