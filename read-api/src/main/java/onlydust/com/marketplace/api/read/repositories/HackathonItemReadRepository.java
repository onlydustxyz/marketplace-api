package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.hackathon.HackathonItemReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface HackathonItemReadRepository extends Repository<HackathonItemReadEntity, UUID> {

    @Query(value = """
            select h.id,
                   h.slug,
                   h.index,
                   h.status,
                   h.title,
                   h.description,
                   h.location,
                   h.start_date,
                   h.end_date,
                   h.github_labels,
                   hic.issue_count,
                   hic.open_issue_count,
                   (select count(hr.user_id)
                    from hackathon_registrations hr
                    where hr.hackathon_id = h.id) registrations_count,
                   (select jsonb_agg(jsonb_build_object('id', p.id, 'name', p.name, 'slug', p.slug, 'logoUrl', p.logo_url))
                    from hackathon_projects hp
                             join projects p on p.id = hp.project_id
                    where hp.hackathon_id = h.id) projects
            from hackathons h
                     left join hackathon_issue_counts hic on hic.hackathon_id = h.id
            where h.status = 'PUBLISHED'
            """, nativeQuery = true)
    Page<HackathonItemReadEntity> findAllPublished(Pageable pageable);
    @Query(value = """
            select h.id,
                   h.slug,
                   h.index,
                   h.status,
                   h.title,
                   h.description,
                   h.location,
                   h.start_date,
                   h.end_date,
                   h.github_labels,
                   hic.issue_count,
                   hic.open_issue_count,
                   (select count(hr.user_id)
                    from hackathon_registrations hr
                    where hr.hackathon_id = h.id) registrations_count,
                   (select jsonb_agg(jsonb_build_object('id', p.id, 'name', p.name, 'slug', p.slug, 'logoUrl', p.logo_url))
                    from hackathon_projects hp
                             join projects p on p.id = hp.project_id
                    where hp.hackathon_id = h.id) projects
            from hackathons h
                     left join hackathon_issue_counts hic on hic.hackathon_id = h.id
            """, nativeQuery = true)
    Page<HackathonItemReadEntity> findAll(Pageable pageable);
}
