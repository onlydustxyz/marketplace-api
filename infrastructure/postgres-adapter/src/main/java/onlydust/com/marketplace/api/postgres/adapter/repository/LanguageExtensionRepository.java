package onlydust.com.marketplace.api.postgres.adapter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.LanguageFileExtensionEntity;

public interface LanguageExtensionRepository extends JpaRepository<LanguageFileExtensionEntity, String> {

    @Query(value = """
            SELECT DISTINCT unnest(main_file_extensions) FROM indexer_exp.github_pull_requests;
            """, nativeQuery = true)
    List<String> findAllIndexed();
}
