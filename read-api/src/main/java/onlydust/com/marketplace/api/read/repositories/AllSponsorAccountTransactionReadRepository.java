package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.accounting.AllSponsorAccountTransactionReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface AllSponsorAccountTransactionReadRepository extends Repository<AllSponsorAccountTransactionReadEntity, UUID> {

    @Query(value = """
            SELECT t
            FROM AllSponsorAccountTransactionReadEntity t
            JOIN FETCH t.sponsor s
            LEFT JOIN FETCH t.program
            JOIN FETCH t.currency c
            LEFT JOIN FETCH c.latestUsdQuote
            WHERE s.id = :sponsorId AND
                    (:types IS NULL OR t.type IN :types)
            """)
    Page<AllSponsorAccountTransactionReadEntity> findAll(@NonNull UUID sponsorId,
                                                         List<AllSponsorAccountTransactionReadEntity.Type> types,
                                                         Pageable pageable);
}
