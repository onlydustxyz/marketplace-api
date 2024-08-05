package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.api.contract.ReadBillingProfilesApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.BillingProfileUserRightsViewRepository;
import onlydust.com.marketplace.api.read.repositories.BillingProfileReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadBillingProfilesApiPostgresAdapter implements ReadBillingProfilesApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final BillingProfileReadRepository billingProfileReadRepository;
    // TODO migrate to read model ?
    private final BillingProfileUserRightsViewRepository billingProfileUserRightsViewRepository;
    private final BillingProfileStoragePort billingProfileStoragePort; // TODO migrate in permission service

    @Override
    public ResponseEntity<BillingProfileResponse> getBillingProfile(UUID billingProfileId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        if (!billingProfileStoragePort.isUserMemberOf(BillingProfile.Id.of(billingProfileId), UserId.of(authenticatedUser.id()))
            && !billingProfileStoragePort.isUserInvitedTo(BillingProfile.Id.of(billingProfileId), GithubUserId.of(authenticatedUser.githubUserId())))
            throw forbidden("User %s does not have permission to read billing profile %s".formatted(authenticatedUser.id(), billingProfileId));

        final var billingProfile = billingProfileReadRepository.findById(billingProfileId)
                .orElseThrow(() -> OnlyDustException.notFound("Billing profile %s not found".formatted(billingProfileId)));

        final var me = billingProfileUserRightsViewRepository.findForUserIdAndBillingProfileId(authenticatedUser.id(), billingProfileId)
                .orElseThrow(() -> OnlyDustException.notFound("Billing profile user rights for user %s and billing profile %s not found".formatted(authenticatedUser.id(), billingProfileId)));

        return ResponseEntity.ok(
                billingProfile.toResponse()
                        .me(me == null ? null :
                                new BillingProfileResponseMe()
                                        .canLeave(me.userProcessingRewardsCount() == 0 && me.userRole() == BillingProfile.User.Role.MEMBER)
                                        .canDelete(me.billingProfileProcessingRewardsCount() == 0 && me.userRole() == BillingProfile.User.Role.ADMIN)
                                        .role(map(me.userRole()))
                                        .invitation(me.invitedByGithubUserId() == null ? null :
                                                new BillingProfileCoworkerInvitation()
                                                        .invitedBy(new ContributorResponse()
                                                                .avatarUrl(me.invitedByGithubAvatarUrl())
                                                                .login(me.invitedByGithubLogin())
                                                                .githubUserId(me.invitedByGithubUserId()))
                                                        .role(map(me.invitedRole()))
                                                        .invitedAt(me.invitedAt())
                                        )
                        )
        );
    }

    private BillingProfileCoworkerRole map(BillingProfile.User.Role role) {
        return switch (role) {
            case ADMIN -> BillingProfileCoworkerRole.ADMIN;
            case MEMBER -> BillingProfileCoworkerRole.MEMBER;
        };
    }
}
