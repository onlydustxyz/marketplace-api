package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeBannersApi;
import onlydust.com.backoffice.api.contract.model.BannerCreateRequest;
import onlydust.com.backoffice.api.contract.model.BannerCreateResponse;
import onlydust.com.marketplace.project.domain.port.input.BannerFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
                bannerCreateRequest.getText(),
                bannerCreateRequest.getButtonText(),
                bannerCreateRequest.getButtonIconSlug(),
                bannerCreateRequest.getButtonLinkUrl());

        return status(HttpStatus.CREATED).body(new BannerCreateResponse().id(banner.id().value()));
    }
}