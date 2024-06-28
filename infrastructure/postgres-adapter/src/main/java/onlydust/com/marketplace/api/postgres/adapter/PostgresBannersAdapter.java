package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.BannerEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BannerRepository;
import onlydust.com.marketplace.project.domain.model.Banner;
import onlydust.com.marketplace.project.domain.port.output.BannerStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
public class PostgresBannersAdapter implements BannerStoragePort {
    private final BannerRepository bannerRepository;

    @Override
    public void save(Banner banner) {
        bannerRepository.save(BannerEntity.of(banner));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Banner> findById(Banner.Id id) {
        return bannerRepository.findById(id.value()).map(BannerEntity::toDomain);
    }

    @Override
    public void delete(Banner.Id id) {
        bannerRepository.deleteById(id.value());
    }

    @Override
    public void hideAll() {
        bannerRepository.findAll().forEach(banner -> banner.visible(false));
    }
}
