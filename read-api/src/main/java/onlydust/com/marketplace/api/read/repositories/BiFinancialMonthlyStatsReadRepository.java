package onlydust.com.marketplace.api.read.repositories;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.read.entities.program.BiFinancialMonthlyStatsReadEntity;
import org.intellij.lang.annotations.Language;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class BiFinancialMonthlyStatsReadRepository {
    @Language("PostgreSQL")
    private static final String SELECT_QUERY = """
            with dates as (select generate_series(date_trunc('month', coalesce(cast(:fromDate as timestamptz), '2020-01-01'), 'UTC'),
                                                  date_trunc('month', coalesce(cast(:toDate as timestamptz), now()), 'UTC'),
                                                  '1 month') as date)
            select gen_random_uuid() as technical_id,
                   d.date,
                   tx.currency_id,
                   coalesce(tx.total_deposited, 0)   as total_deposited,
                   coalesce(tx.total_allocated, 0)   as total_allocated,
                   coalesce(tx.total_granted, 0)     as total_granted,
                   coalesce(tx.total_rewarded, 0)    as total_rewarded,
                   coalesce(tx.total_paid, 0)        as total_paid,
                   coalesce(tx.transaction_count, 0) as transaction_count
            from dates d
                     left join lateral (select #group_by#                                                                                                                 as id,
                                               tx.currency_id                                                                                                             as currency_id,
            
                                               coalesce(sum(tx.amount) filter ( where type = 'DEPOSIT' and tx.deposit_status = 'COMPLETED' ), 0)
                                                   - coalesce(sum(tx.amount) filter ( where type = 'WITHDRAW' and tx.program_id is null ), 0)                             as total_deposited,
            
                                               coalesce(sum(tx.amount) filter ( where type = 'TRANSFER' and tx.program_id is not null and tx.project_id is null ), 0)
                                                   - coalesce(sum(tx.amount) filter ( where type = 'REFUND' and tx.program_id is not null and tx.project_id is null ), 0) as total_allocated,
            
                                               coalesce(sum(tx.amount) filter ( where type = 'TRANSFER' and tx.project_id is not null and tx.reward_id is null ), 0)
                                                   - coalesce(sum(tx.amount) filter ( where type = 'REFUND' and tx.project_id is not null and tx.reward_id is null ), 0)  as total_granted,
            
                                               coalesce(sum(tx.amount) filter ( where type = 'TRANSFER' and tx.reward_id is not null and tx.payment_id is null ), 0)
                                                   - coalesce(sum(tx.amount) filter ( where type = 'REFUND' and tx.reward_id is not null and tx.payment_id is null ), 0)  as total_rewarded,
            
                                               coalesce(sum(tx.amount) filter ( where type = 'BURN' and tx.reward_id is not null ), 0)                                    as total_paid,
            
                                               count(tx.id)                                                                                                               as transaction_count
                                        from accounting.all_transactions tx
                                                 left join rewards r on r.id = tx.reward_id
                                                 left join sponsors s on s.id = tx.sponsor_id
                                                 left join programs pgm on pgm.id = tx.program_id
                                                 left join projects p on p.id = tx.project_id
                                                 left join iam.all_indexed_users rr on rr.github_user_id = r.recipient_id
                                        where #group_by# = :id
                                          and tx.timestamp >= d.date
                                          and tx.timestamp < d.date + interval '1 month'
                                          and tx.type in ('DEPOSIT', 'WITHDRAW', 'TRANSFER', 'REFUND', 'BURN')
                                          and (
                                            ('DEPOSITED' in (:types) and tx.type = 'DEPOSIT' and tx.deposit_status != 'DRAFT') or
                                            ('ALLOCATED' in (:types) and tx.type = 'TRANSFER' and tx.program_id is not null and tx.project_id is null) or
                                            ('UNALLOCATED' in (:types) and tx.type = 'REFUND' and tx.program_id is not null and tx.project_id is null) or
                                            ('GRANTED' in (:types) and tx.type = 'TRANSFER' and tx.project_id is not null and tx.reward_id is null) or
                                            ('UNGRANTED' in (:types) and tx.type = 'REFUND' and tx.project_id is not null and tx.reward_id is null) or
                                            ('REWARDED' in (:types) and tx.type = 'TRANSFER' and tx.reward_id is not null and tx.payment_id is null) or
                                            ('PAID' in (:types) and tx.type = 'BURN' and tx.reward_id is not null)
                                            )
                                          and (cast(:search as text) is null or (concat(s.name, ' ', pgm.name, ' ', p.name, ' ', rr.login) ilike '%' || :search || '%'))
                                          and (cast(:searchProjectsAndRecipients as text) is null or (concat(p.name, ' ', rr.login) ilike '%' || :searchProjectsAndRecipients || '%'))
                                        group by 1, 2) tx
                               on true
            order by d.date
            """;

    private final EntityManager entityManager;

    public List<BiFinancialMonthlyStatsReadEntity> findAll(UUID id,
                                                           Long recipientId,
                                                           IdGrouping groupBy,
                                                           ZonedDateTime fromDate,
                                                           ZonedDateTime toDate,
                                                           String search,
                                                           @NonNull List<String> types) {
        final var result = groupBy.findAll(entityManager, id, recipientId, fromDate, toDate, search,
                types.isEmpty() ? List.of("no-type") : types);
        return result.stream().dropWhile(s -> fromDate == null && s.currencyId() == null).toList();
    }

    @AllArgsConstructor
    public enum IdGrouping {
        SPONSOR_ID("tx"), PROGRAM_ID("tx"), PROJECT_ID("tx"), RECIPIENT_ID("r");

        final String tableAlias;

        List<BiFinancialMonthlyStatsReadEntity> findAll(EntityManager entityManager,
                                                        UUID id,
                                                        Long recipientId,
                                                        ZonedDateTime fromDate,
                                                        ZonedDateTime toDate,
                                                        String search,
                                                        List<String> types) {
            final var query = entityManager.createNativeQuery(SELECT_QUERY.replaceAll("#group_by#", tableAlias + "." + name().toLowerCase()),
                    BiFinancialMonthlyStatsReadEntity.class);
            switch (this) {
                case SPONSOR_ID, PROGRAM_ID, PROJECT_ID -> {
                    query.setParameter("id", id);
                    query.setParameter("search", search);
                    query.setParameter("searchProjectsAndRecipients", null);
                }
                case RECIPIENT_ID -> {
                    query.setParameter("id", recipientId);
                    query.setParameter("search", null);
                    query.setParameter("searchProjectsAndRecipients", search);
                }
            }
            query.setParameter("fromDate", fromDate);
            query.setParameter("toDate", toDate);
            query.setParameter("types", types);
            return query.getResultList();
        }
    }
}
