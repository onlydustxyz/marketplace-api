package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;

import java.util.Date;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class Kyb {
    @NonNull
    UUID id;
    @NonNull
    UserId ownerId;
    @NonNull
    VerificationStatus status;
    @NonNull
    BillingProfile.Id billingProfileId;
    String name;
    String registrationNumber;
    Date registrationDate;
    String address;
    Country country;
    Boolean usEntity;
    Boolean subjectToEuropeVAT;
    String euVATNumber;
    String reviewMessageForApplicant;
    String externalApplicantId;

    public static Kyb initForUserAndBillingProfile(final UserId userId, final BillingProfile.Id billingProfileId) {
        return Kyb.builder()
                .id(UUID.randomUUID())
                .ownerId(userId)
                .billingProfileId(billingProfileId)
                .status(VerificationStatus.NOT_STARTED)
                .build();
    }

    public String sumsubUrl() {
        return externalApplicantId == null ? null :
                "https://cockpit.sumsub.com/checkus/#/applicant/%s/basicInfo?clientId=onlydust".formatted(externalApplicantId);
    }
}
