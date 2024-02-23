package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import onlydust.com.marketplace.project.domain.view.UserRewardStatus;
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
    @NonNull UUID rewardId;

    @Enumerated(EnumType.STRING)
    @Type(type = "reward_status")
    @NonNull Status status;

    public enum Status {
        PENDING_BILLING_PROFILE, PENDING_VERIFICATION, PAYMENT_BLOCKED, PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST, PROCESSING, COMPLETE
    }

    public UserRewardStatus forUser() {
        return switch (status) {
            case PENDING_BILLING_PROFILE -> UserRewardStatus.missingPayoutInfo; // TODO add dedicated status
            case PENDING_VERIFICATION -> UserRewardStatus.pendingVerification;
            case PAYMENT_BLOCKED -> UserRewardStatus.locked;// TODO add dedicated status
            case PAYOUT_INFO_MISSING -> UserRewardStatus.missingPayoutInfo;
            case LOCKED -> UserRewardStatus.locked;
            case PENDING_REQUEST -> UserRewardStatus.pendingInvoice;// TODO add dedicated status
            case PROCESSING -> UserRewardStatus.processing;
            case COMPLETE -> UserRewardStatus.complete;
        };
    }
}
