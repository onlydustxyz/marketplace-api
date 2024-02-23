package onlydust.com.marketplace.project.domain.model.notification;

import lombok.*;
import onlydust.com.marketplace.kernel.jobs.OutboxSkippingException;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;
import onlydust.com.marketplace.project.domain.model.OldBillingProfileType;
import onlydust.com.marketplace.project.domain.model.OldVerificationStatus;

import java.util.UUID;

import static java.util.Objects.nonNull;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("BillingProfileUpdated")
public class BillingProfileUpdated extends Event {
    UUID billingProfileId;
    OldBillingProfileType type;
    OldVerificationStatus oldVerificationStatus;
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
            if (this.type.equals(OldBillingProfileType.INDIVIDUAL)) {
                return true;
            } else {
                throw new OutboxSkippingException("Invalid children billing profile for type %s and external parent id %s"
                        .formatted(this.type, this.parentExternalApplicantId));
            }
        }
        return false;
    }
}
