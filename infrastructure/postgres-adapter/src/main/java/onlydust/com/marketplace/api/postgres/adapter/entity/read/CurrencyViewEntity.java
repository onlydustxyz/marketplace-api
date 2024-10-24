package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.kernel.model.CurrencyView;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.net.URI;
import java.util.UUID;

@Entity
@Table(name = "currencies", schema = "public")
@NoArgsConstructor(force = true)
@Getter
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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
    private int cmcId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "currency_id", insertable = false, updatable = false)
    LatestUsdQuoteViewEntity latestUsdQuote;

    public Currency toDomain() {
        return Currency.builder()
                .id(Currency.Id.of(id))
                .type(type)
                .name(name)
                .code(Currency.Code.of(code))
                .metadata(new Currency.Metadata(cmcId, name, description, logoUrl == null ? null : URI.create(logoUrl)))
                .decimals(decimals)
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
