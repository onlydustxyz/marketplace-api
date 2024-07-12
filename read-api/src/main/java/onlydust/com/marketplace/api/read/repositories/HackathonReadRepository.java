package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface HackathonReadRepository extends Repository<HackathonReadEntity, UUID> {

    @Query("""
            select h from HackathonReadEntity h
                left join fetch h.sponsors
                left join fetch h.projects
                left join fetch h.registrations
            where h.id = :id
            """)
    @NonNull
    Optional<HackathonReadEntity> findById(@NonNull UUID id);

    @Query("""
            select h from HackathonReadEntity h
                left join fetch h.sponsors
                left join fetch h.projects
                left join fetch h.registrations
            where h.slug = :slug
            """)
    @NonNull
    Optional<HackathonReadEntity> findBySlug(@NonNull String slug);

    @Query(nativeQuery = true, value = """
                select count(*) > 0 from hackathon_registrations where user_id = :userId and hackathon_id = :hackathonId
            """)
    Boolean isRegisteredToHackathon(UUID userId, UUID hackathonId);

    @Query("""
            select h from HackathonReadEntity h
            where h.status = 'PUBLISHED'
            """)
    Page<HackathonReadEntity> findAllPublished(Pageable pageable);

    Page<HackathonReadEntity> findAll(Pageable pageable);
}
