package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorLeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SponsorLeadRepository extends JpaRepository<SponsorLeadEntity, SponsorLeadEntity.PrimaryKey> {

    @Query(value = """
            select s.*
            from sponsor_leads s
            join programs p on p.sponsor_id = s.sponsor_id and p.id = :programId
            where s.user_id = :userId
            """, nativeQuery = true)
    Optional<SponsorLeadEntity> findByUserIdAndProgramId(UUID userId, UUID programId);

    List<SponsorLeadEntity> findBySponsorId(UUID sponsorId);

    List<SponsorLeadEntity> findByUserId(UUID userId);
}
