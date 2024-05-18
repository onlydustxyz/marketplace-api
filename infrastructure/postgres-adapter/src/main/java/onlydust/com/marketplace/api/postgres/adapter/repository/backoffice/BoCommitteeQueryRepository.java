package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoCommitteeQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BoCommitteeQueryRepository extends JpaRepository<BoCommitteeQueryEntity, BoCommitteeQueryRepository> {

    @Query(nativeQuery = true, value = """
                select c.id,
                       c.name,
                       c.status,
                       c.start_date,
                       c.end_date,
                       c.project_questions,
                       s.name as sponsor_name,
                       s.logo_url as sponsor_logo_url
                from committees c
                left join sponsors s on s.id = c.sponsor_id
                where c.id = :committeeId
            """)
    Optional<BoCommitteeQueryEntity> findById(UUID committeeId);
}
