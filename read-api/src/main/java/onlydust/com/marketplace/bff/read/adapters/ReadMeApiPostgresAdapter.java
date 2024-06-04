package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadMeApi;
import onlydust.com.marketplace.api.contract.model.MyBillingProfilesResponse;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.bff.read.entities.billing_profile.AllBillingProfileUserReadEntity;
import onlydust.com.marketplace.bff.read.repositories.AllBillingProfileUserReadRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadMeApiPostgresAdapter implements ReadMeApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final AllBillingProfileUserReadRepository allBillingProfileUserReadRepository;

    @Override
    public ResponseEntity<MyBillingProfilesResponse> getMyBillingProfiles() {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var billingProfiles = allBillingProfileUserReadRepository.findAllByUserId(authenticatedUser.getId());

        final var response = new MyBillingProfilesResponse()
                .billingProfiles(billingProfiles.stream().map(AllBillingProfileUserReadEntity::toShortResponse).toList());
        return ResponseEntity.ok(response);
    }
}
