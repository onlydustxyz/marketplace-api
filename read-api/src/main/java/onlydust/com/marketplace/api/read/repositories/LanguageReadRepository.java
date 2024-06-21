package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface LanguageReadRepository extends Repository<LanguageReadEntity, UUID> {
    @Query("""
            SELECT DISTINCT l
            FROM LanguageReadEntity l
            JOIN l.ecosystems e
            WHERE e.slug = :ecosystemSlug
            """)
    Page<LanguageReadEntity> findAllByEcosystemSlug(String ecosystemSlug, Pageable pageable);
}
