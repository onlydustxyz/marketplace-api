package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadBannerApi;
import onlydust.com.marketplace.api.contract.model.BannerResponse;
import onlydust.com.marketplace.api.read.properties.Cache;
import onlydust.com.marketplace.api.read.repositories.BannerReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.UserId;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static onlydust.com.marketplace.api.read.properties.Cache.S;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadBannerApiPostgresAdapter implements ReadBannerApi {
    private final Cache cache;
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final BannerReadRepository bannerReadRepository;

    @Override
    public ResponseEntity<BannerResponse> getBanner(Boolean hiddenIgnoredByMe) {
        final var authenticatedUser = authenticatedAppUserService.tryGetAuthenticatedUser();
        final Boolean hiddeIgnoredByMeFilter = Optional.ofNullable(hiddenIgnoredByMe).orElse(Boolean.FALSE);

        final var banner = hiddeIgnoredByMeFilter ?
                bannerReadRepository.findMyFirstVisibleBanner(authenticatedUser.map(AuthenticatedUser::id).map(UserId::value).orElse(null))
                : bannerReadRepository.findFirstVisibleBanner();

        return banner.map(bannerReadEntity -> ok()
                        .cacheControl(cache.whenAnonymous(authenticatedUser, S))
                        .body(bannerReadEntity.toResponse()))
                .orElseGet(() -> noContent()
                        .cacheControl(cache.whenAnonymous(authenticatedUser, S))
                        .build());
    }
}
