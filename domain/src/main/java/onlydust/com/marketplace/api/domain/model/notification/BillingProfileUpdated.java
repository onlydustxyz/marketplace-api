package onlydust.com.marketplace.api.domain.model.notification;

import lombok.*;
import onlydust.com.marketplace.api.domain.job.OutboxSkippingException;
import onlydust.com.marketplace.api.domain.model.BillingProfileType;
import onlydust.com.marketplace.api.domain.model.VerificationStatus;

import java.util.UUID;

import static java.util.Objects.nonNull;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BillingProfileUpdated extends Event {
    UUID billingProfileId;
    BillingProfileType type;
    VerificationStatus verificationStatus;
    String reviewMessageForApplicant;
    UUID userId;
    Long githubUserId;
    String githubUserEmail;
    String githubLogin;
    String githubAvatarUrl;
    String rawReviewDetails;
    String externalApplicantId;
    String parentExternalApplicantId;

    public boolean isLinkedToAParentBillingProfile() {
        if (nonNull(this.parentExternalApplicantId)) {
            if (this.type.equals(BillingProfileType.INDIVIDUAL)) {
                return true;
            } else {
                throw new OutboxSkippingException("Invalid children billing profile for type %s and external parent id %s"
                        .formatted(this.type, this.parentExternalApplicantId));
            }
        }
        return false;
    }
}
