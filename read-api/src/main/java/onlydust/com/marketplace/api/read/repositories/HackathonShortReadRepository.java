package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.hackathon.HackathonShortReadEntity;
import org.intellij.lang.annotations.Language;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface HackathonShortReadRepository extends Repository<HackathonShortReadEntity, UUID> {
    @Language("PostgreSQL")
    String SELECT = """
            SELECT
                h.id,
                h.slug,
                h.status,
                h.title,
                h.location,
                h.start_date,
                h.end_date,
                (SELECT jsonb_agg(jsonb_build_object(
                        'id', p.id,
                        'slug', p.slug,
                        'name', p.name,
                        'logoUrl', p.logo_url,
                        'shortDescription', p.short_description,
                        'visibility', p.visibility))
                 FROM projects p
                    JOIN (SELECT jsonb_array_elements_text(jsonb_array_elements(h.tracks) -> 'projectIds')) tracks(projects) ON p.id = cast(tracks.projects as uuid)
                 ) as projects
            FROM
                hackathons h
            """;

    @Language("PostgreSQL")
    String COUNT = """
            SELECT
                count(1)
            FROM
                hackathons h
            """;

    @Query(value = SELECT + """
            WHERE
                h.status = 'PUBLISHED'
            """,
            countQuery = COUNT + """
                    WHERE
                        h.status = 'PUBLISHED'
                    """, nativeQuery = true)
    Page<HackathonShortReadEntity> findAllPublished(Pageable pageable);

    @Query(value = SELECT, countQuery = COUNT, nativeQuery = true)
    Page<HackathonShortReadEntity> findAll(Pageable pageable);

    @Query(nativeQuery = true, value = """
                select count(*) > 0 from hackathon_registrations where user_id = :userId and hackathon_id = :hackathonId
            """)
    Boolean isRegisteredToHackathon(UUID userId, UUID hackathonId);
}
