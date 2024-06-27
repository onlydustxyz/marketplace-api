package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BannerEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BannerRepository;
import onlydust.com.marketplace.project.domain.model.Banner;
import onlydust.com.marketplace.project.domain.port.output.BannerStoragePort;

@AllArgsConstructor
public class PostgresBannersAdapter implements BannerStoragePort {
    private final BannerRepository bannerRepository;

    @Override
    public void save(Banner banner) {
        bannerRepository.save(BannerEntity.of(banner));
    }
}
