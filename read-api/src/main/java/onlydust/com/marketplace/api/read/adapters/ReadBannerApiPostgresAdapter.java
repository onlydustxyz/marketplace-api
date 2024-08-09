package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadBannerApi;
import onlydust.com.marketplace.api.contract.model.BannerResponse;
import onlydust.com.marketplace.api.read.entities.BannerReadEntity;
import onlydust.com.marketplace.api.read.repositories.BannerReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadBannerApiPostgresAdapter implements ReadBannerApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final BannerReadRepository bannerReadRepository;

    @Override
    public ResponseEntity<BannerResponse> getBanner(Boolean hiddeIgnoredByMe) {
        final var authenticatedUser = authenticatedAppUserService.tryGetAuthenticatedUser();
        final Boolean hiddeIgnoredByMeFilter = Optional.ofNullable(hiddeIgnoredByMe).orElse(Boolean.FALSE);

        final var banner = hiddeIgnoredByMeFilter ?
                bannerReadRepository.findMyFirstVisibleBanner(authenticatedUser.map(AuthenticatedUser::id).orElse(null))
                : bannerReadRepository.findFirstVisibleBanner();

        return banner.map(BannerReadEntity::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
