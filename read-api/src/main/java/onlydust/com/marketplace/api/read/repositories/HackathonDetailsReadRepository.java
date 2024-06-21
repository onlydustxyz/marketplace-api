package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonDetailsReadEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface HackathonDetailsReadRepository extends JpaRepository<HackathonDetailsReadEntity, UUID> {


    @Language("PostgreSQL")
    String SELECT = """
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
                        'id', p.id,
                        'slug', p.slug,
                        'name', p.name,
                        'logoUrl', p.logo_url,
                        'shortDescription', p.short_description,
                        'visibility', p.visibility))
                 FROM projects p
                    JOIN (SELECT jsonb_array_elements_text(jsonb_array_elements(h.tracks) -> 'projectIds')) tracks(projects) ON p.id = cast(tracks.projects as uuid)
                 ) as projects,
                 (SELECT count(distinct u.id)
                      FROM hackathon_registrations hr
                               JOIN iam.users u ON u.id = hr.user_id
                      WHERE hr.hackathon_id = h.id)                             registered_users_count
            FROM
                hackathons h
            """;

    @Query(value = SELECT + " WHERE h.id = :id ", nativeQuery = true)
    @NonNull
    Optional<HackathonDetailsReadEntity> findById(@NonNull UUID id);

    @Query(value = SELECT + " WHERE h.slug = :slug ", nativeQuery = true)
    Optional<HackathonDetailsReadEntity> findBySlug(String slug);
}
