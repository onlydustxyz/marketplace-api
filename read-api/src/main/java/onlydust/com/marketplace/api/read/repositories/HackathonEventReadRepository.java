package onlydust.com.marketplace.api.read.repositories;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import onlydust.com.marketplace.api.read.entities.hackathon.HackathonEventReadEntity;

public interface HackathonEventReadRepository extends Repository<HackathonEventReadEntity, UUID> {

    @Query(value = """
        select  e.id,
                e.hackathon_id,
                e.name,
                e.subtitle,
                e.icon_slug,
                e.start_at,
                e.end_at,
                e.links
            from hackathon_events e
                    join hackathons h on e.hackathon_id = h.id
            where h.slug = :hackathonSlug
            and (cast(:fromDate as timestamptz) is null or e.start_at >= cast(:fromDate as timestamptz))
            and (cast(:toDate as timestamptz) is null or e.end_at < cast(:toDate as timestamptz))
            order by e.start_at desc
        """, nativeQuery = true)
    List<HackathonEventReadEntity> findHackathonEvents(String hackathonSlug, ZonedDateTime fromDate, ZonedDateTime toDate);
}
