package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface LanguageRepository extends JpaRepository<LanguageEntity, UUID> {

    @Modifying
    @Query(nativeQuery = true, value = """
            REFRESH MATERIALIZED VIEW CONCURRENTLY project_languages;
            """)
    void updateProjectLanguages();
}
