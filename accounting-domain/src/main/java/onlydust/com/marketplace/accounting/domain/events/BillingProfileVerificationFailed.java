package onlydust.com.marketplace.accounting.domain.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@EventType("BillingProfileVerificationFailed")
public class BillingProfileVerificationFailed extends Event {
    @NonNull
    String ownerEmail;
    @NonNull
    BillingProfile.Id billingProfileId;
    @NonNull
    String ownerGithubLogin;
    @NonNull
    VerificationStatus verificationStatus;
}
