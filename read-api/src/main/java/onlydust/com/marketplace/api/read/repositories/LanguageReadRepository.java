package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface LanguageReadRepository extends Repository<LanguageReadEntity, UUID> {
    @Query("""
            SELECT DISTINCT l
            FROM LanguageReadEntity l
            JOIN l.ecosystems e
            WHERE e.slug = :ecosystemSlug
            """)
    Page<LanguageReadEntity> findAllByEcosystemSlug(String ecosystemSlug, Pageable pageable);

    @Query(value = """
            SELECT DISTINCT l.*
            FROM languages l
                     JOIN project_languages pl ON pl.language_id = l.id
                     JOIN hackathon_projects hp ON hp.project_id = pl.project_id
                     JOIN hackathon_issues h ON h.hackathon_id = hp.hackathon_id
                     JOIN indexer_exp.github_issues i ON i.id = h.issue_id
                     JOIN repo_languages rl ON rl.repo_id = i.repo_id AND rl.language_id = l.id
            WHERE hp.hackathon_id = :hackathonId
            """, nativeQuery = true)
    List<LanguageReadEntity> findAllByHackathonId(UUID hackathonId);
}
