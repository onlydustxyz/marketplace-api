package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import jakarta.persistence.*;
import lombok.*;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.hibernate.annotations.Type;

import java.util.UUID;

@Entity
@Value
@Table(name = "reward_statuses", schema = "accounting")
@NoArgsConstructor(force = true)
public class RewardStatusEntity {
    @Id
    @Getter(AccessLevel.NONE)
    @NonNull
    UUID rewardId;

    @Getter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Type(PostgreSQLEnumType.class)
    @Column(columnDefinition = "reward_status")
    @NonNull
    Status status;

    public enum Status {
        PENDING_SIGNUP, PENDING_BILLING_PROFILE, PENDING_VERIFICATION, GEO_BLOCKED, INDIVIDUAL_LIMIT_REACHED, PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST,
        PROCESSING, COMPLETE;
    }

    public static Status from(RewardStatus.Input rewardStatus) {
        return switch (rewardStatus) {
            case PENDING_SIGNUP -> Status.PENDING_SIGNUP;
            case PENDING_BILLING_PROFILE -> Status.PENDING_BILLING_PROFILE;
            case PENDING_VERIFICATION -> Status.PENDING_VERIFICATION;
            case GEO_BLOCKED -> Status.GEO_BLOCKED;
            case INDIVIDUAL_LIMIT_REACHED -> Status.INDIVIDUAL_LIMIT_REACHED;
            case PAYOUT_INFO_MISSING -> Status.PAYOUT_INFO_MISSING;
            case LOCKED -> Status.LOCKED;
            case PENDING_REQUEST -> Status.PENDING_REQUEST;
            case PROCESSING -> Status.PROCESSING;
            case COMPLETE -> Status.COMPLETE;
        };
    }

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
