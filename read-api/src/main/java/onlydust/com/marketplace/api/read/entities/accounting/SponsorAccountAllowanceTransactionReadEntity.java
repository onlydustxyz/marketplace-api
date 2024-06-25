package onlydust.com.marketplace.api.read.entities.accounting;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "sponsor_account_allowance_transactions", schema = "accounting")
@Immutable
@Accessors(fluent = true)
public class SponsorAccountAllowanceTransactionReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @ManyToOne
    @JoinColumn(name = "accountId")
    @NonNull
    SponsorAccountReadEntity account;

    @NonNull
    @Enumerated(EnumType.STRING)
    SponsorAccount.AllowanceTransaction.Type type;

    @NonNull
    BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "projectId")
    @NonNull
    ProjectReadEntity project;
}
