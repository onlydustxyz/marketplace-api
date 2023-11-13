package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLedIdViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProjectLedIdRepository extends JpaRepository<ProjectLedIdViewEntity, UUID> {

    @Query(value = """
            select pl.user_id,
                   pl.project_id,
                   pd.key as project_slug
            from project_leads pl
            left join project_details pd on pd.project_id = pl.project_id
            where pl.user_id = :userId""", nativeQuery = true)
    List<ProjectLedIdViewEntity> findProjectLedIdsByUserId(final UUID userId);
}
