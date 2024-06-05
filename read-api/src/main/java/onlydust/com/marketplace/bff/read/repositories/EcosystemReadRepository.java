package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.ecosystem.EcosystemReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface EcosystemReadRepository extends Repository<EcosystemReadEntity, UUID> {
    @Query("""
            SELECT e 
            FROM EcosystemReadEntity e
            JOIN FETCH e.mdBanner
            JOIN FETCH e.xlBanner
            LEFT JOIN FETCH e.articles
            WHERE e.slug = :slug
            """)
    Optional<EcosystemReadEntity> findBySlug(String slug);

    @Query("""
            SELECT e 
            FROM EcosystemReadEntity e
            JOIN FETCH e.mdBanner
            JOIN FETCH e.xlBanner
            WHERE (:hidden IS NULL OR e.hidden = :hidden)
            """)
    Page<EcosystemReadEntity> findAll(Boolean hidden, Pageable pageable);

    @Query("""
            SELECT e 
            FROM EcosystemReadEntity e
            JOIN FETCH e.mdBanner
            JOIN FETCH e.xlBanner
            WHERE e.featuredRank IS NOT NULL
            AND (:hidden IS NULL OR e.hidden = :hidden)
            """)
    Page<EcosystemReadEntity> findAllFeatured(Boolean hidden, Pageable pageable);
}
