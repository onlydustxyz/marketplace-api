package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.project.domain.model.Banner;
import onlydust.com.marketplace.project.domain.port.input.BannerFacadePort;
import onlydust.com.marketplace.project.domain.port.output.BannerStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class BannerService implements BannerFacadePort {
    private final BannerStoragePort bannerStoragePort;

    @Override
    public Banner createBanner(String shortDescription, String longDescription, String title, String subTitle, ZonedDateTime date, String buttonText,
                               String buttonIconSlug, URI buttonLinkUrl) {
        final var banner = new Banner(shortDescription, longDescription, title, subTitle, buttonText, buttonIconSlug, buttonLinkUrl, date);
        bannerStoragePort.save(banner);
        return banner;
    }

    @Override
    public void updateBanner(Banner.Id id, String shortDescription, String longDescription, String title, String subTitle, ZonedDateTime date,
                             String buttonText, String buttonIconSlug, URI buttonLinkUrl) {
        final var banner = bannerStoragePort.findById(id)
                .orElseThrow(() -> notFound("Banner %s not found".formatted(id)));

        bannerStoragePort.save(banner
                .shortDescription(shortDescription)
                .longDescription(longDescription)
                .title(title)
                .subTitle(subTitle)
                .date(date)
                .buttonText(buttonText)
                .buttonIconSlug(buttonIconSlug)
                .buttonLinkUrl(buttonLinkUrl)
                .updatedAt(ZonedDateTime.now()));
    }

    @Override
    public void deleteBanner(Banner.Id id) {
        final var banner = bannerStoragePort.findById(id)
                .orElseThrow(() -> notFound("Banner %s not found".formatted(id)));

        bannerStoragePort.delete(banner.id());
    }

    @Override
    public void hideBanner(Banner.Id id) {
        final var banner = bannerStoragePort.findById(id)
                .orElseThrow(() -> notFound("Banner %s not found".formatted(id)));

        bannerStoragePort.save(banner.visible(false));
    }

    @Override
    @Transactional
    public void showBanner(Banner.Id id) {
        bannerStoragePort.hideAll();
        final var banner = bannerStoragePort.findById(id)
                .orElseThrow(() -> notFound("Banner %s not found".formatted(id)));

        bannerStoragePort.save(banner.visible(true));
    }

    @Override
    public void closeBanner(Banner.Id id, UUID userId) {
        final var banner = bannerStoragePort.findById(id)
                .orElseThrow(() -> notFound("Banner %s not found".formatted(id)));

        banner.close(userId);
        bannerStoragePort.save(banner);
    }
}
