package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface LanguageReadRepository extends Repository<LanguageReadEntity, UUID>, JpaRepository<LanguageReadEntity, UUID> {
    @Query("""
            SELECT DISTINCT l
            FROM LanguageReadEntity l
            JOIN l.ecosystems e
            WHERE e.slug = :ecosystemSlug
            """)
    Page<LanguageReadEntity> findAllByEcosystemSlug(String ecosystemSlug, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT l.*
            FROM hackathon_projects hp
                     JOIN bi.p_project_global_data p ON p.project_id = hp.project_id
                     JOIN languages l ON l.id = ANY (p.language_ids)
            WHERE hp.hackathon_id = :hackathonId
            """, nativeQuery = true)
    List<LanguageReadEntity> findAllByHackathonId(UUID hackathonId);

    List<LanguageReadEntity> findAllByNameContainingIgnoreCase(String search, Sort sort);

    @Query(value = """
            SELECT DISTINCT l.*
            FROM languages l
            JOIN user_profile_info upi ON l.id = ANY (upi.preferred_language_ids) and upi.id = :userId
            """, nativeQuery = true)
    List<LanguageReadEntity> findPreferredOnesForUser(UUID userId);
}
