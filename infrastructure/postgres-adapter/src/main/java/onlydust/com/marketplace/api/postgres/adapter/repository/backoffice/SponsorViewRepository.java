package onlydust.com.marketplace.api.postgres.adapter.repository.backoffice;

import onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read.SponsorViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SponsorViewRepository extends JpaRepository<SponsorViewEntity, UUID> {


    @Query(value = """
            select s.id,
                   s.name,
                   s.logo_url,
                   s.url,
                   linked_projects.projects
            from sponsors s
                     left join (select ps.sponsor_id,
                                       json_agg(json_build_object('name', pd.name, 'logoUrl', pd.logo_url, 'id', pd.project_id, 'slug', pd.key, 'shortDescription', pd.short_description)) projects
                                from projects_sponsors ps
                                         join project_details pd on pd.project_id = ps.project_id
                                group by ps.sponsor_id) linked_projects on linked_projects.sponsor_id = s.id
            order by s.name
            """, nativeQuery = true)
    Page<SponsorViewEntity> findAll(Pageable pageable);
}
