package onlydust.com.marketplace.bff.read.repositories;

import onlydust.com.marketplace.bff.read.entities.hackathon.HackathonShortReadEntity;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface HackathonShortReadRepository extends JpaRepository<HackathonShortReadEntity, UUID> {
    Page<HackathonShortReadEntity> findByStatusIn(Set<Hackathon.Status> statuses, Pageable pageable);

    @Query(nativeQuery = true, value = """
                select count(*) > 0 from hackathon_registrations where user_id = :userId and hackathon_id = :hackathonId
            """)
    Boolean isRegisteredToHackathon(UUID userId, UUID hackathonId);
}
