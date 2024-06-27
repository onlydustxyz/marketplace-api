package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.model.Banner;
import onlydust.com.marketplace.project.domain.port.input.BannerFacadePort;
import onlydust.com.marketplace.project.domain.port.output.BannerStoragePort;

import java.net.URI;

@AllArgsConstructor
public class BannerService implements BannerFacadePort {
    private final BannerStoragePort bannerStoragePort;

    @Override
    public Banner createBanner(String text, String buttonText, String buttonIconSlug, URI buttonLinkUrl) {
        final var banner = new Banner(text, buttonText, buttonIconSlug, buttonLinkUrl);
        bannerStoragePort.save(banner);
        return banner;
    }
}
