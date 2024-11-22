package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.cache.QueryCacheS;
import onlydust.com.marketplace.api.read.entities.BannerReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface BannerReadRepository extends Repository<BannerReadEntity, UUID> {
    Page<BannerReadEntity> findAll(Pageable pageable);

    Optional<BannerReadEntity> findById(UUID bannerId);

    @Query("""
            SELECT b
            FROM BannerReadEntity b
            WHERE b.visible = true
            AND (:userId IS NULL OR NOT EXISTS (
                SELECT 1
                FROM BannerClosedByEntity c
                WHERE c.bannerId = b.id
                AND c.userId = :userId
            ))
            """)
    @QueryCacheS
    Optional<BannerReadEntity> findMyFirstVisibleBanner(UUID userId);

    @Query("""
            SELECT b
            FROM BannerReadEntity b
            WHERE b.visible = true
            """)
    @QueryCacheS
    Optional<BannerReadEntity> findFirstVisibleBanner();


}
