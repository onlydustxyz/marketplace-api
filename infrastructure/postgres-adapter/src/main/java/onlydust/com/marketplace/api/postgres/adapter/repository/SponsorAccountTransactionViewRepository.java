package onlydust.com.marketplace.api.postgres.adapter.repository;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.HistoricalTransaction;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.SponsorAccountTransactionViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SponsorAccountTransactionViewRepository extends JpaRepository<SponsorAccountTransactionViewEntity, UUID> {
    @Query(value = """
            SELECT t.*
            FROM accounting.all_sponsor_account_transactions t
            JOIN accounting.sponsor_accounts sa ON sa.id = t.sponsor_account_id
            JOIN currencies c ON sa.currency_id = c.id
            LEFT JOIN project_details p ON p.project_id = t.project_id
            WHERE
                sa.sponsor_id = :sponsorId AND
                (coalesce(:currencyIds) IS NULL OR sa.currency_id IN (:currencyIds) ) AND
                (coalesce(:projectIds) IS NULL OR t.project_id IN (:projectIds) ) AND
                (coalesce(:types) IS NULL OR CAST(t.type AS TEXT) IN (:types) ) AND
                (coalesce(:fromDate) IS NULL OR t.timestamp >= TO_DATE(CAST(:fromDate AS TEXT), 'YYYY-MM-DD') ) AND
                (coalesce(:toDate) IS NULL OR t.timestamp < TO_DATE(CAST(:toDate AS TEXT), 'YYYY-MM-DD') + 1 )
            """, nativeQuery = true)
    Page<SponsorAccountTransactionViewEntity> findAll(UUID sponsorId,
                                                      List<UUID> currencyIds,
                                                      List<UUID> projectIds,
                                                      List<String> types,
                                                      String fromDate,
                                                      String toDate,
                                                      Pageable pageable);

    static Sort sortBy(@NonNull HistoricalTransaction.Sort sort, @NonNull Sort.Direction direction) {
        return switch (sort) {
            case DATE -> Sort.by(direction, "timestamp");
            case TYPE -> Sort.by(direction, "type");
            case AMOUNT -> Sort.by(direction, "c.code", "amount");
            case PROJECT -> Sort.by(direction, "p.name");
        };
    }
}
