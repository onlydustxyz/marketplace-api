package onlydust.com.marketplace.accounting.domain.view;

import lombok.*;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
@Getter
public class BillingProfileView {
    BillingProfile.Id id;
    BillingProfile.Type type;
    String name;
    Kyc kyc;
    Kyb kyb;
    BillingProfileUserRightsView me;
    VerificationStatus verificationStatus;
    PayoutInfo payoutInfo;
    Boolean enabled;
    List<User> admins;

    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateAcceptedAt;
    @Getter(AccessLevel.NONE)
    ZonedDateTime invoiceMandateLatestVersionDate;
    Integer rewardCount;
    Integer invoiceableRewardCount;
    Boolean missingPayoutInfo;
    Boolean missingVerification;
    Boolean individualLimitReached;

    PositiveAmount currentYearPaymentLimit;
    PositiveAmount currentYearPaymentAmount;

    List<TotalMoneyView> currentMonthRewardedAmounts;

    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED;
    }

    public boolean isInvoiceMandateAccepted() {
        if (type == BillingProfile.Type.INDIVIDUAL) return true;

        return invoiceMandateAcceptedAt != null &&
                invoiceMandateLatestVersionDate != null &&
                invoiceMandateAcceptedAt.isAfter(invoiceMandateLatestVersionDate);
    }

    public boolean isSwitchableToSelfEmployed() {
        return this.type == BillingProfile.Type.COMPANY && this.me.billingProfileCoworkersCount() == 0;
    }

    public boolean isVerificationBlocked() {
        return verificationStatus.isBlocked();
    }

    public String subject() {
        return kyc == null ? kyb.getName() : kyc.fullName();
    }

    public record User(
            @NonNull UserId id,
            @NonNull GithubUserId githubUserId,
            @NonNull String githubLogin,
            @NonNull URI githubAvatarUrl,
            @NonNull String email
    ) {
    }
}
