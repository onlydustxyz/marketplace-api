package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.postgres.adapter.entity.write.HistoricalTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface HistoricalTransactionRepository extends JpaRepository<HistoricalTransactionEntity, UUID> {
    @Query(value = """
            SELECT 
                sat.id                                                       AS id,
                sat.tech_created_at                                          AS timestamp,
                'DEPOSIT'                                                    AS type,
                sat.account_id                                               AS sponsor_account_id,
                sat.amount                                                   AS amount,
                accounting.usd_quote_at(sa.currency_id, sat.tech_created_at) AS usd_conversion_rate,
                NULL                                                         AS project_id
            FROM
                accounting.sponsor_accounts sa
            JOIN accounting.sponsor_account_transactions sat ON sat.account_id = sa.id AND sat.type = 'DEPOSIT'
            WHERE
                sa.sponsor_id = :sponsorId 
            UNION
            SELECT 
                gen_random_uuid()   AS id,
                abe.tech_created_at AS timestamp,
                'ALLOCATION'        AS type,
                CAST(abe.payload #>> '{event, from, id}' AS UUID)            AS sponsor_account_id,
                CAST(abe.payload #>> '{event, amount, value}' AS numeric)    AS amount,
                accounting.usd_quote_at(ab.currency_id, abe.tech_created_at) AS usd_conversion_rate,
                CAST(abe.payload #>> '{event, to, id}' AS UUID)              AS project_id
            FROM
                accounting.sponsor_accounts sa
            JOIN accounting.account_books_events abe ON 
                CAST(abe.payload #>> '{event, from, id}' AS UUID) = sa.id AND
                abe.payload #>> '{event, @type}' = 'Transfer' AND
                abe.payload #>> '{event, to, type}' = 'PROJECT'
            JOIN accounting.account_books ab on ab.id = abe.account_book_id
            WHERE 
                sa.sponsor_id = :sponsorId
            """, nativeQuery = true)
    Page<HistoricalTransactionEntity> findAll(UUID sponsorId, Pageable pageable);
}
