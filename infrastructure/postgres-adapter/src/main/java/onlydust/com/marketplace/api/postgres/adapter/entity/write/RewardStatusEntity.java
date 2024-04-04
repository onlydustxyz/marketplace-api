package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Value
@Table(name = "reward_statuses", schema = "accounting")
@TypeDef(name = "reward_status", typeClass = PostgreSQLEnumType.class)
@NoArgsConstructor(force = true)
public class RewardStatusEntity {
    @Id
    @Getter(AccessLevel.NONE)
    @NonNull
    UUID rewardId;

    @Getter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Type(type = "reward_status")
    @NonNull
    Status status;

    public enum Status {
        PENDING_SIGNUP, PENDING_BILLING_PROFILE, PENDING_VERIFICATION, GEO_BLOCKED, INDIVIDUAL_LIMIT_REACHED, PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST,
        PROCESSING, COMPLETE;
    }

    public static Status from(RewardStatus rewardStatus) {
        return switch (rewardStatus) {
            case PENDING_SIGNUP -> Status.PENDING_SIGNUP;
            case PENDING_CONTRIBUTOR, PENDING_COMPANY -> null;
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

    public RewardStatus toDomain() {
        return switch (status) {
            case PENDING_SIGNUP -> RewardStatus.PENDING_SIGNUP;
            case PENDING_BILLING_PROFILE -> RewardStatus.PENDING_BILLING_PROFILE;
            case PENDING_VERIFICATION -> RewardStatus.PENDING_VERIFICATION;
            case GEO_BLOCKED -> RewardStatus.GEO_BLOCKED;
            case INDIVIDUAL_LIMIT_REACHED -> RewardStatus.INDIVIDUAL_LIMIT_REACHED;
            case PAYOUT_INFO_MISSING -> RewardStatus.PAYOUT_INFO_MISSING;
            case LOCKED -> RewardStatus.LOCKED;
            case PENDING_REQUEST -> RewardStatus.PENDING_REQUEST;
            case PROCESSING -> RewardStatus.PROCESSING;
            case COMPLETE -> RewardStatus.COMPLETE;
        };
    }
}
