package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.HackathonDetailsViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface HackathonDetailsViewRepository extends JpaRepository<HackathonDetailsViewEntity, UUID> {

    @Query(value = """
            SELECT
                h.id,
                h.slug,
                h.status,
                h.title,
                h.subtitle,
                h.description,
                h.location,
                h.budget,
                h.start_date,
                h.end_date,
                h.links,
                h.tracks,
                (SELECT jsonb_agg(jsonb_build_object(
                        'id', s.id,
                        'name', s.name,
                        'logoUrl', s.logo_url,
                        'url', s.url))
                 FROM sponsors s WHERE s.id = ANY(h.sponsor_ids)) as sponsors,
                (SELECT jsonb_agg(jsonb_build_object(
                        'id', p.project_id,
                        'name', p.name,
                        'logoUrl', p.logo_url,
                        'shortDescription', p.short_description,
                        'slug', p.key))
                 FROM project_details p
                    JOIN (SELECT jsonb_array_elements_text(jsonb_array_elements(h.tracks) -> 'projectIds')) tracks(projects) ON p.project_id = cast(tracks.projects as uuid)
                 ) as projects
            FROM
                hackathons h
            WHERE
                h.id = :id
            """, nativeQuery = true)
    @NonNull
    Optional<HackathonDetailsViewEntity> findById(@NonNull UUID id);
}
