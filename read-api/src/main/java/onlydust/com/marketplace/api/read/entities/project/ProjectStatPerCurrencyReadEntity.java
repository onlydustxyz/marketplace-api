package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionStat;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor(force = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@Entity
@Table(name = "project_stats_per_currency", schema = "bi")
@IdClass(ProjectStatPerCurrencyReadEntity.PrimaryKey.class)
public class ProjectStatPerCurrencyReadEntity implements ProgramTransactionStat {
    @Id
    @NonNull
    UUID projectId;

    @Id
    @NonNull
    UUID currencyId;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    ProjectReadEntity project;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    ProjectStatReadEntity stats;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "currencyId", insertable = false, updatable = false)
    CurrencyReadEntity currency;

    @NonNull
    BigDecimal totalGranted;

    @NonNull
    BigDecimal totalRewarded;

    @Override
    public String toString() {
        return "ProjectStatPerCurrencyReadEntity{" +
               "projectId=" + projectId +
               ", currencyId=" + currencyId +
               ", totalGranted=" + totalGranted +
               ", totalRewarded=" + totalRewarded +
               '}';
    }

    @EqualsAndHashCode
    public static class PrimaryKey {
        UUID projectId;
        UUID currencyId;
    }
}