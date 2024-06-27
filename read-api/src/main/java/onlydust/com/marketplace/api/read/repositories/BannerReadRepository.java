package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.BannerReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface BannerReadRepository extends Repository<BannerReadEntity, UUID> {
    Page<BannerReadEntity> findAll(Pageable pageable);
}
