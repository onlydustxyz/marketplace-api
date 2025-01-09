package onlydust.com.marketplace.api.read.repositories;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import onlydust.com.marketplace.api.read.entities.hackathon.HackathonEventReadEntity;

public interface HackathonEventReadRepository extends Repository<HackathonEventReadEntity, UUID> {

    @Query(value = """
        with start_event as (select gen_random_uuid()                                                  as id,
                                    id                                                                 as hackathon_id,
                                    'ODHack begins'                                                    as name,
                                    'Get ready to start contributing, connecting & receiving rewards!' as subtitle,
                                    'ri-calendar-line'                                                 as icon_slug,
                                    start_date                                                         as start_at,
                                    start_date                                                         as end_at,
                                    cast('[]' as jsonb)                                                as links
                            from hackathons
                            where slug = :hackathonSlug),
            end_event as (select gen_random_uuid()                                                               as id,
                                 id                                                                              as hackathon_id,
                                 'ODHack finishes'                                                               as name,
                                 'All tasks should have been completed, now maintainers will review final work.' as subtitle,
                                 'ri-calendar-line'                                                              as icon_slug,
                                 end_date                                                                        as start_at,
                                 end_date                                                                        as end_at,
                                 cast('[]' as jsonb)                                                             as links
                            from hackathons
                            where slug = :hackathonSlug),
            custom_events as (select e.id,
                                    e.hackathon_id,
                                    e.name,
                                    e.subtitle,
                                    e.icon_slug,
                                    e.start_at,
                                    e.end_at,
                                    e.links
                            from hackathon_events e
                                        join hackathons h on e.hackathon_id = h.id
                            where h.slug = :hackathonSlug),
            all_events as (select *
                            from start_event
                            union
                            select *
                            from end_event
                            union
                            select *
                            from custom_events)
        select *
        from all_events e
        where (cast(:fromDate as timestamptz) is null or e.end_at >= cast(:fromDate as timestamptz))
        and (cast(:toDate as timestamptz) is null or e.start_at <= cast(:toDate as timestamptz))
        order by e.start_at desc
        """, nativeQuery = true)
    List<HackathonEventReadEntity> findHackathonEvents(String hackathonSlug, ZonedDateTime fromDate, ZonedDateTime toDate);
}
