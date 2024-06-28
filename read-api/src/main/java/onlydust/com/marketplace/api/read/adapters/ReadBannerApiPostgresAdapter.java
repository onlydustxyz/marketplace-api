package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadBannerApi;
import onlydust.com.marketplace.api.contract.model.BannerResponse;
import onlydust.com.marketplace.api.read.entities.BannerReadEntity;
import onlydust.com.marketplace.api.read.repositories.BannerReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.project.domain.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadBannerApiPostgresAdapter implements ReadBannerApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final BannerReadRepository bannerReadRepository;


    @Override
    public ResponseEntity<BannerResponse> getBanner() {
        final var authenticatedUser = authenticatedAppUserService.tryGetAuthenticatedUser();
        final var banner = bannerReadRepository.findFirstVisibleBanner(authenticatedUser.map(User::getId).orElse(null));

        return banner.map(BannerReadEntity::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
