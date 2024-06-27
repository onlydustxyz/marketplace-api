package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.Banner;

import java.util.Optional;

public interface BannerStoragePort {

    void save(Banner banner);

    Optional<Banner> findById(Banner.Id id);
}
