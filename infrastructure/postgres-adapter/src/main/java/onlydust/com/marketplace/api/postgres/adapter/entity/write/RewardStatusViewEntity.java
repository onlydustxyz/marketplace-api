package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.UUID;

@Entity
@Value
@Table(name = "reward_statuses", schema = "accounting")
@NoArgsConstructor(force = true)
@Immutable
public class RewardStatusViewEntity {
    @Id
    @Getter(AccessLevel.NONE)
    @NonNull
    UUID rewardId;

    @Getter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "reward_status")
    @NonNull
    RewardStatusEntity.Status status;

    public RewardStatus.Input toDomain() {
        return switch (status) {
            case PENDING_SIGNUP -> RewardStatus.Input.PENDING_SIGNUP;
            case PENDING_BILLING_PROFILE -> RewardStatus.Input.PENDING_BILLING_PROFILE;
            case PENDING_VERIFICATION -> RewardStatus.Input.PENDING_VERIFICATION;
            case GEO_BLOCKED -> RewardStatus.Input.GEO_BLOCKED;
            case INDIVIDUAL_LIMIT_REACHED -> RewardStatus.Input.INDIVIDUAL_LIMIT_REACHED;
            case PAYOUT_INFO_MISSING -> RewardStatus.Input.PAYOUT_INFO_MISSING;
            case LOCKED -> RewardStatus.Input.LOCKED;
            case PENDING_REQUEST -> RewardStatus.Input.PENDING_REQUEST;
            case PROCESSING -> RewardStatus.Input.PROCESSING;
            case COMPLETE -> RewardStatus.Input.COMPLETE;
        };
    }
}
