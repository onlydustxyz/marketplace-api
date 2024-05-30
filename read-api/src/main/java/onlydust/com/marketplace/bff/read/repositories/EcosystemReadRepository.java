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
            WHERE e.slug = :slug
            """)
    Optional<EcosystemReadEntity> findBySlug(String slug);

    @Query("""
            SELECT e 
            FROM EcosystemReadEntity e
            JOIN FETCH e.mdBanner
            JOIN FETCH e.xlBanner
            """)
    Page<EcosystemReadEntity> findAll(Pageable pageable);

    @Query("""
            SELECT e 
            FROM EcosystemReadEntity e
            JOIN FETCH e.mdBanner
            JOIN FETCH e.xlBanner
            WHERE e.featuredRank IS NOT NULL
            """)
    Page<EcosystemReadEntity> findAllFeatured(Pageable pageable);
}
