package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.accounting.AccountBookTransactionReadEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface AccountBookTransactionReadRepository extends Repository<AccountBookTransactionReadEntity, UUID> {
    @Query(value = """
            SELECT t
            FROM AccountBookTransactionReadEntity t
            JOIN FETCH t.sponsor
            JOIN FETCH t.currency
            LEFT JOIN FETCH t.project p
            WHERE
                t.program.id = :programId AND
                t.reward IS NULL AND
                (:search IS NULL OR p.name ilike '%' || CAST(:search AS String) || '%' OR t.program.name ilike '%' || CAST(:search AS String) || '%') AND
                (CAST(:fromDate AS String) IS NULL OR t.timestamp >= :fromDate) AND
                (CAST(:toDate AS String) IS NULL OR DATE_TRUNC('DAY', t.timestamp) <= :toDate) AND
                (COALESCE(:types, NULL) IS NULL OR (
                    'GRANTED' in (:types) and CAST(t.type AS String) in ('TRANSFER', 'REFUND') and t.project is not null or
                    'RECEIVED' in (:types) and CAST(t.type AS String) = 'TRANSFER' and t.project is null or
                    'RETURNED' in (:types) and CAST(t.type AS String) = 'REFUND' and t.project is null
                ))
            """)
    Page<AccountBookTransactionReadEntity> findAllForProgram(UUID programId,
                                                             Date fromDate,
                                                             Date toDate,
                                                             String search,
                                                             List<String> types,
                                                             Pageable pageable);

    @Query(value = """
            SELECT t
            FROM AccountBookTransactionReadEntity t
            JOIN FETCH t.sponsor
            JOIN FETCH t.currency
            LEFT JOIN FETCH t.program
            WHERE
                t.sponsor.id = :sponsorId AND
                t.project IS NULL AND
                (:search IS NULL OR t.program.name ilike '%' || CAST(:search AS String) || '%') AND
                (CAST(:fromDate AS String) IS NULL OR t.timestamp >= :fromDate) AND
                (CAST(:toDate AS String) IS NULL OR DATE_TRUNC('DAY', t.timestamp) <= :toDate) AND
                (COALESCE(:types, NULL) IS NULL OR (
                    'DEPOSITED' in (:types) and CAST(t.type AS String) = 'MINT' and t.program is null or
                    'ALLOCATED' in (:types) and CAST(t.type AS String) = 'TRANSFER' and t.program is not null or
                    'RETURNED' in (:types) and CAST(t.type AS String) = 'REFUND' and t.program is not null
                ))
            """)
    Page<AccountBookTransactionReadEntity> findAllForSponsor(UUID sponsorId,
                                                             Date fromDate,
                                                             Date toDate,
                                                             String search,
                                                             List<String> types,
                                                             Pageable pageable);
}
