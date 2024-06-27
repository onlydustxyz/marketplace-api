package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.Banner;

import java.net.URI;

public interface BannerFacadePort {
    Banner createBanner(String text, String buttonText, String buttonIconSlug, URI buttonLinkUrl);
}
