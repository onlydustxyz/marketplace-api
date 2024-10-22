package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.api.helper.UserAuthHelper.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Set;

@Service
public class BillingProfileHelper {

    protected static final Faker faker = new Faker();

    @Autowired
    BillingProfileFacadePort billingProfileFacadePort;

    @Autowired
    BillingProfileStoragePort billingProfileStoragePort;

    public BillingProfile verify(AuthenticatedUser user, Country country) {
        return verify(user, country, null);
    }

    public BillingProfile verify(AuthenticatedUser user, Country country, Set<ProjectId> selectForProjects) {
        final var bp = billingProfileFacadePort.createIndividualBillingProfile(UserId.of(user.user().getId()), "Individual", selectForProjects);
        billingProfileStoragePort.saveKyc(bp.kyc().toBuilder()
                .country(country)
                .status(VerificationStatus.VERIFIED)
                .build());
        billingProfileStoragePort.updateBillingProfileStatus(bp.id(), VerificationStatus.VERIFIED);
        return bp;
    }

    public BillingProfile verifyCompany(AuthenticatedUser user, Country country, Set<ProjectId> selectForProjects, Set<UserId> admins, Set<UserId> members) {
        final var bp = billingProfileFacadePort.createCompanyBillingProfile(UserId.of(user.user().getId()), "Company", selectForProjects);
        billingProfileStoragePort.saveKyb(bp.kyb().toBuilder()
                .country(country)
                .status(VerificationStatus.VERIFIED)
                .usEntity(false)
                .subjectToEuropeVAT(false)
                .name("Company Inc.")
                .address("123 Main St")
                .build());
        billingProfileStoragePort.updateBillingProfileStatus(bp.id(), VerificationStatus.VERIFIED);
        admins.forEach(userId -> billingProfileStoragePort.saveCoworker(bp.id(), userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()));
        members.forEach(userId -> billingProfileStoragePort.saveCoworker(bp.id(), userId, BillingProfile.User.Role.MEMBER, ZonedDateTime.now()));
        return bp;
    }

    public void addPayoutInfo(BillingProfile.Id billingProfileId, PayoutInfo payoutInfo) {
        billingProfileStoragePort.savePayoutInfoForBillingProfile(payoutInfo, billingProfileId);
    }

    public void selectForProject(UserId userId, BillingProfile.Id billingProfileId, ProjectId projectId) {
        billingProfileStoragePort.savePayoutPreference(billingProfileId, userId, projectId);
    }
}
