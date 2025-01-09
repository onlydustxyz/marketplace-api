package onlydust.com.marketplace.api.read.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonV2ReadEntity;

public interface HackathonV2ReadRepository extends Repository<HackathonV2ReadEntity, UUID> {

    @Query(value = """
            select  id                                as id,
                    slug                              as slug,
                    title                             as title,
                    description                       as description,
                    location                          as location,
                    start_date                        as start_date,
                    end_date                          as end_date,
                    community_links                   as community_links,
                    links                             as links,
                    coalesce(hic.issue_count, 0)      as issue_count,
                    coalesce(hic.open_issue_count, 0) as available_issue_count,
                    coalesce(hr.user_count, 0)        as subscriber_count,
                    coalesce(hp.project_count, 0)     as project_count
                from hackathons h
                        left join hackathon_issue_counts hic on h.id = hic.hackathon_id
                        left join (select hackathon_id,
                                        count(user_id) as user_count
                                    from hackathon_registrations
                                    group by hackathon_id) hr on h.id = hr.hackathon_id
                        left join (select hackathon_id,
                                        count(project_id) as project_count
                                    from hackathon_projects
                                    group by hackathon_id) hp on h.id = hp.hackathon_id
                where slug = :slug
            """, nativeQuery = true)
    @NonNull
    Optional<HackathonV2ReadEntity> findBySlug(String slug);
}
