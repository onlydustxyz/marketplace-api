package onlydust.com.marketplace.api.domain.model.notification;

import lombok.*;
import onlydust.com.marketplace.api.domain.model.BillingProfileType;
import onlydust.com.marketplace.api.domain.model.VerificationStatus;

import java.util.UUID;

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
}
