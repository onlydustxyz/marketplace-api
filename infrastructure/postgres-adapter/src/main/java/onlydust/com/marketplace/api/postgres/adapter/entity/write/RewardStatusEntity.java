package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.project.domain.view.ProjectRewardView;
import onlydust.com.marketplace.project.domain.view.UserRewardView;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Value
@Table(name = "reward_statuses", schema = "accounting")
@TypeDef(name = "reward_status", typeClass = PostgreSQLEnumType.class)
@TypeDef(name = "reward_status_as_project_lead", typeClass = PostgreSQLEnumType.class)
@NoArgsConstructor(force = true)
public class RewardStatusEntity {
    @Id
    @NonNull UUID rewardId;

    @Getter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Type(type = "reward_status")
    @NonNull Status statusForUser;

    @Getter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Type(type = "reward_status_as_project_lead")
    @NonNull StatusAsProjectLead statusForProjectLead;


    public enum Status {
        PENDING_BILLING_PROFILE, PENDING_VERIFICATION, PAYMENT_BLOCKED, PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST, PROCESSING, COMPLETE;

        // TODO: Wtf? Why do we have many different status enums?
        public static Status from(BackofficeRewardView.Status status) {
            return switch (status) {
                case PENDING_INVOICE -> Status.PENDING_REQUEST;
                case PENDING_SIGNUP -> Status.PAYMENT_BLOCKED;//TODO?
                case PENDING_CONTRIBUTOR -> Status.PAYMENT_BLOCKED;//TODO?
                case PENDING_VERIFICATION -> Status.PENDING_VERIFICATION;
                case MISSING_PAYOUT_INFO -> Status.PAYOUT_INFO_MISSING;
                case PROCESSING -> Status.PROCESSING;
                case COMPLETE -> Status.COMPLETE;
                case LOCKED -> Status.LOCKED;
            };
        }
    }

    public enum StatusAsProjectLead {
        PENDING_SIGNUP, PENDING_CONTRIBUTOR, PROCESSING, COMPLETE
    }

    public UserRewardView.Status forUser() {
        return switch (statusForUser) {
            case PENDING_BILLING_PROFILE -> UserRewardView.Status.missingPayoutInfo; // TODO add dedicated status
            case PENDING_VERIFICATION -> UserRewardView.Status.pendingVerification;
            case PAYMENT_BLOCKED -> UserRewardView.Status.locked;// TODO add dedicated status
            case PAYOUT_INFO_MISSING -> UserRewardView.Status.missingPayoutInfo;
            case LOCKED -> UserRewardView.Status.locked;
            case PENDING_REQUEST -> UserRewardView.Status.pendingInvoice;// TODO add dedicated status
            case PROCESSING -> UserRewardView.Status.processing;
            case COMPLETE -> UserRewardView.Status.complete;
        };
    }

    public ProjectRewardView.Status forProjectLead() {
        return switch (statusForProjectLead) {
            case PENDING_SIGNUP -> ProjectRewardView.Status.pendingSignup;
            case PENDING_CONTRIBUTOR -> ProjectRewardView.Status.pendingContributor;
            case PROCESSING -> ProjectRewardView.Status.processing;
            case COMPLETE -> ProjectRewardView.Status.complete;
        };
    }
}
