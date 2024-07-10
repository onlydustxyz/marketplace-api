package onlydust.com.marketplace.api.read.repositories;

import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.accounting.SponsorAccountAllowanceTransactionReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface SponsorAccountAllowanceTransactionReadRepository extends Repository<SponsorAccountAllowanceTransactionReadEntity, UUID> {

    @Query(value = """
            SELECT t
            FROM SponsorAccountAllowanceTransactionReadEntity t
            JOIN FETCH t.account sa
            LEFT JOIN FETCH t.project p
            JOIN FETCH sa.currency c
            LEFT JOIN FETCH c.latestUsdQuote
            WHERE sa.sponsor.id = :sponsorId AND
                    (:types IS NULL OR t.type IN :types) AND
                    (:currencies IS NULL OR c.id IN :currencies) AND
                    (:projects IS NULL OR p.id IN :projects) AND
                    (CAST(:fromDate AS String) IS NULL OR t.timestamp >= :fromDate) AND
                    (CAST(:toDate AS String) IS NULL OR DATE_TRUNC('DAY', t.timestamp) <= :toDate)
            """)
    Page<SponsorAccountAllowanceTransactionReadEntity> findAll(@NonNull UUID sponsorId,
                                                               List<SponsorAccountAllowanceTransactionReadEntity.Type> types,
                                                               List<UUID> currencies,
                                                               List<UUID> projects,
                                                               Date fromDate,
                                                               Date toDate,
                                                               Pageable pageable);
}
