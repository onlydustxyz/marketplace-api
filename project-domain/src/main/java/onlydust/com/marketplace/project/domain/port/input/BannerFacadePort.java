package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Banner;

import java.net.URI;
import java.time.ZonedDateTime;

public interface BannerFacadePort {
    Banner createBanner(String shortDescription, String longDescription, String title, String subTitle, ZonedDateTime date, String buttonText,
                        String buttonIconSlug, URI buttonLinkUrl);

    void updateBanner(Banner.Id id, String shortDescription, String longDescription, String title, String subTitle, ZonedDateTime date, String buttonText,
                      String buttonIconSlug, URI buttonLinkUrl);

    void deleteBanner(Banner.Id id);

    void hideBanner(Banner.Id id);

    void showBanner(Banner.Id id);

    void closeBanner(Banner.Id id, UserId userId);
}
