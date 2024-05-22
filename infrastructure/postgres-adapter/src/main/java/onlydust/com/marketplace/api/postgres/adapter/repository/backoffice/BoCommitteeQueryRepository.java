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
                       (select jsonb_agg(
                               jsonb_build_object('id', cpq.id, 'question', cpq.question, 'required', cpq.required)
                       ) from committee_project_questions cpq
                         where cpq.committee_id = c.id
                        ) project_questions,
                       s.id as sponsor_id,
                       s.name as sponsor_name,
                       s.url as sponsor_url,
                       s.logo_url as sponsor_logo_url
                from committees c
                left join sponsors s on s.id = c.sponsor_id
                where c.id = :committeeId
            """)
    Optional<BoCommitteeQueryEntity> findById(UUID committeeId);
}
