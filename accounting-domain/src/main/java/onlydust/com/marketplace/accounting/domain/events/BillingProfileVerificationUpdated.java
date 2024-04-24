package onlydust.com.marketplace.accounting.domain.events;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationType;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.kernel.jobs.OutboxSkippingException;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EventType("BillingProfileVerificationUpdated")
public class BillingProfileVerificationUpdated extends Event {
    UUID verificationId;
    @NonNull
    VerificationType type;
    @NonNull
    VerificationStatus verificationStatus;
    String reviewMessageForApplicant;
    UserId userId;
    String rawReviewDetails;
    @NonNull
    String externalApplicantId;
    String parentExternalApplicantId;

    public boolean isAChildrenKYC() {
        if (nonNull(this.parentExternalApplicantId)) {
            if (this.type.equals(VerificationType.KYC)) {
                return true;
            } else {
                throw new OutboxSkippingException("Invalid children billing profile for type %s and external parent id %s"
                        .formatted(this.type, this.parentExternalApplicantId));
            }
        }
        return false;
    }

    public boolean failed() {
        return List.of(VerificationStatus.REJECTED, VerificationStatus.CLOSED).contains(this.verificationStatus);
    }
}
