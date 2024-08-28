package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.sponsor.SponsorReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface SponsorReadRepository extends Repository<SponsorReadEntity, UUID> {
    @Query("""
            SELECT s
            FROM SponsorReadEntity s
            LEFT JOIN FETCH s.statsPerCurrency
            LEFT JOIN FETCH s.leads
            WHERE s.id = :sponsorId
            """)
    Optional<SponsorReadEntity> findById(UUID sponsorId);

    @Query("""
            SELECT s
            FROM SponsorReadEntity s
            LEFT JOIN FETCH s.programs
            WHERE :search is null or lower(s.name) LIKE lower(concat('%', cast(:search as String), '%'))
            """)
    Page<SponsorReadEntity> findAllByName(String search, Pageable pageable);
}
