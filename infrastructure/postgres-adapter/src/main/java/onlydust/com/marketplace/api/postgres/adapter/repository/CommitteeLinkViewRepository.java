package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.read.CommitteeLinkViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CommitteeLinkViewRepository extends JpaRepository<CommitteeLinkViewEntity, UUID> {

    @Query(nativeQuery = true, value = """
            select c.id,
                   c.application_start_date,
                   c.application_end_date,
                   c.name,
                   c.status,
                   c.tech_created_at,
                   coalesce(pc.count,0) project_count
                   from committees c
                   left join (
                    select cpa.committee_id, count(cpa.project_id) count
                       from committee_project_answers cpa
                       group by cpa.committee_id
                   ) pc on pc.committee_id = c.id
                   order by c.tech_created_at desc
            """)
    Page<CommitteeLinkViewEntity> findAllBy(Pageable pageable);

}
