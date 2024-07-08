package onlydust.com.marketplace.api.read.entities.reward;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.backoffice.api.contract.model.RewardStatusContract;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

import static onlydust.com.backoffice.api.contract.model.RewardStatusContract.*;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "reward_statuses", schema = "accounting")
@NoArgsConstructor(force = true)
@Immutable
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RewardStatusReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID rewardId;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "accounting.reward_status")
    @NonNull
    RewardStatus.Input status;

    RewardStatusContract toBoContract() {
        return switch (status) {
            case LOCKED -> LOCKED;
            case PENDING_BILLING_PROFILE -> PENDING_BILLING_PROFILE;
            case PENDING_VERIFICATION -> PENDING_VERIFICATION;
            case COMPLETE -> COMPLETE;
            case GEO_BLOCKED -> GEO_BLOCKED;
            case INDIVIDUAL_LIMIT_REACHED -> INDIVIDUAL_LIMIT_REACHED;
            case PAYOUT_INFO_MISSING -> PAYOUT_INFO_MISSING;
            case PENDING_REQUEST -> PENDING_REQUEST;
            case PENDING_SIGNUP -> PENDING_SIGNUP;
            case PROCESSING -> PROCESSING;
        };
    }
}
