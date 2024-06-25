package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.ConvertedAmount;
import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.view.ProjectShortView;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.NetworkEnumEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;


@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@NoArgsConstructor(force = true)
@Table(schema = "accounting", name = "all_sponsor_account_transactions")
@Immutable
public class SponsorAccountTransactionViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;
    @NonNull
    ZonedDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "transaction_type")
    @NonNull
    HistoricalTransaction.Type type;

    @ManyToOne
    @NonNull
    SponsorAccountViewEntity sponsorAccount;

    @ManyToOne
    ProjectViewEntity project;

    @NonNull
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "network")
    NetworkEnumEntity network;

    public HistoricalTransaction toDomain() {

        return new HistoricalTransaction(
                id,
                timestamp,
                type,
                Amount.of(amount),
                sponsorAccount.getCurrency().toDomain(),
                isNull(network) ? null : network.toNetwork(),
                isNull(sponsorAccount.getLockedUntil()) ? null : ZonedDateTime.ofInstant(sponsorAccount.getLockedUntil(), ZoneOffset.UTC),
                Optional.ofNullable(sponsorAccount.getCurrency().latestUsdQuote())
                        .map(usdConversionRate -> new ConvertedAmount(Amount.of(amount.multiply(usdConversionRate.getPrice())), usdConversionRate.getPrice()))
                        .orElse(null),
                project == null ? null : ProjectShortView.builder()
                        .id(ProjectId.of(project.getId()))
                        .name(project.getName())
                        .logoUrl(project.getLogoUrl())
                        .slug(project.getSlug())
                        .shortDescription(project.getShortDescription())
                        .build()
        );
    }
}
