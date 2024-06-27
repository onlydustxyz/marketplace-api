package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.Banner;

public interface BannerStoragePort {

    void save(Banner banner);
}
