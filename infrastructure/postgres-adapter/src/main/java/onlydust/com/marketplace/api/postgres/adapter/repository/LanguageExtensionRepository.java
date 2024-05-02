package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.LanguageFileExtensionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface LanguageExtensionRepository extends JpaRepository<LanguageFileExtensionEntity, UUID> {

    @Query(value = """
            SELECT DISTINCT unnest(main_file_extensions) FROM indexer_exp.github_pull_requests;
            """, nativeQuery = true)
    List<String> findAllIndexed();
}
