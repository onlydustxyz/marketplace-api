package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.ConvertedAmount;
import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectEntity;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@TypeDef(name = "transaction_type", typeClass = PostgreSQLEnumType.class)
public class HistoricalTransactionEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID id;

    private @NonNull ZonedDateTime timestamp;

    @NonNull String type;

    @ManyToOne
    SponsorAccountEntity sponsorAccount;

    @NonNull BigDecimal amount;

    BigDecimal usdConversionRate;

    @ManyToOne
    @JoinColumn(name = "project_id")
    ProjectEntity project;

    public HistoricalTransaction toDomain() {
        final var sponsorAccount = this.sponsorAccount.toDomain();

        return new HistoricalTransaction(
                timestamp,
                HistoricalTransaction.Type.valueOf(type),
                sponsorAccount,
                Amount.of(amount),
                usdConversionRate == null ? null : new ConvertedAmount(Amount.of(amount.multiply(usdConversionRate)), usdConversionRate),
                project == null ? null : ShortProjectView.builder()
                        .id(ProjectId.of(project.getId()))
                        .name(project.getName())
                        .logoUrl(project.getLogoUrl())
                        .slug(project.getKey())
                        .shortDescription(project.getShortDescription())
                        .build()
        );
    }
}
