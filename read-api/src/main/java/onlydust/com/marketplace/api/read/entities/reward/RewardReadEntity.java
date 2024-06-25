package onlydust.com.marketplace.api.read.entities.reward;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.read.entities.currency.CurrencyReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@NoArgsConstructor(force = true)
@Table(name = "rewards", schema = "public")
@Accessors(fluent = true)
@Immutable
public class RewardReadEntity {
    @Id
    @NonNull
    UUID id;
    @NonNull
    BigDecimal amount;
    @NonNull
    Date requestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestorId", referencedColumnName = "userId")
    @NonNull
    AllUserReadEntity requestor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipientId", referencedColumnName = "githubUserId")
    @NonNull
    AllUserReadEntity recipient;

    UUID billingProfileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currencyId")
    @NonNull
    CurrencyReadEntity currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId")
    @NonNull
    ProjectReadEntity project;
}
