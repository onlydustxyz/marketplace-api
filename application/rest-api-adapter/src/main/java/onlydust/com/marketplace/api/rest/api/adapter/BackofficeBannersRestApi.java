package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeBannersApi;
import onlydust.com.backoffice.api.contract.model.BannerCreateRequest;
import onlydust.com.backoffice.api.contract.model.BannerCreateResponse;
import onlydust.com.backoffice.api.contract.model.BannerUpdateRequest;
import onlydust.com.marketplace.project.domain.model.Banner;
import onlydust.com.marketplace.project.domain.port.input.BannerFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.status;

@RestController
@Tags(@Tag(name = "BackofficeBanners"))
@AllArgsConstructor
@Profile("bo")
public class BackofficeBannersRestApi implements BackofficeBannersApi {
    private final BannerFacadePort bannerFacadePort;

    @Override
    public ResponseEntity<BannerCreateResponse> createBanner(BannerCreateRequest bannerCreateRequest) {
        final var banner = bannerFacadePort.createBanner(
                bannerCreateRequest.getShortDescription(),
                bannerCreateRequest.getLongDescription(),
                bannerCreateRequest.getTitle(),
                bannerCreateRequest.getSubTitle(),
                bannerCreateRequest.getDate(),
                bannerCreateRequest.getButtonText(),
                bannerCreateRequest.getButtonIconSlug(),
                bannerCreateRequest.getButtonLinkUrl());

        return status(HttpStatus.CREATED).body(new BannerCreateResponse().id(banner.id().value()));
    }

    @Override
    public ResponseEntity<Void> deleteBanner(UUID bannerId) {
        bannerFacadePort.deleteBanner(Banner.Id.of(bannerId));

        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> hideBanner(UUID bannerId) {
        bannerFacadePort.hideBanner(Banner.Id.of(bannerId));

        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> showBanner(UUID bannerId) {
        bannerFacadePort.showBanner(Banner.Id.of(bannerId));

        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateBanner(UUID bannerId, BannerUpdateRequest bannerUpdateRequest) {
        bannerFacadePort.updateBanner(
                Banner.Id.of(bannerId),
                bannerUpdateRequest.getShortDescription(),
                bannerUpdateRequest.getLongDescription(),
                bannerUpdateRequest.getTitle(),
                bannerUpdateRequest.getSubTitle(),
                bannerUpdateRequest.getDate(),
                bannerUpdateRequest.getButtonText(),
                bannerUpdateRequest.getButtonIconSlug(),
                bannerUpdateRequest.getButtonLinkUrl());

        return noContent().build();
    }
}
