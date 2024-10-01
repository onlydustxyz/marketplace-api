package onlydust.com.marketplace.api.read.repositories;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.read.entities.program.BiFinancialMonthlyStatsReadEntity;
import org.intellij.lang.annotations.Language;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class BiFinancialMonthlyStatsReadRepository {
    private final EntityManager entityManager;

    @Language("PostgreSQL")
    private static final String SELECT_QUERY = """
            with data as (select generate_series(date_trunc('month', coalesce(cast(:fromDate as timestamptz), min(tx.timestamp), now()), 'UTC'),
                                                 date_trunc('month', coalesce(cast(:toDate as timestamptz), now()), 'UTC'),
                                                 '1 mon') as date,
                                        tx.#group_by#  as id,
                                        tx.currency_id as currency_id
                                    from accounting.all_transactions tx
                                    where tx.#group_by# = :id
                                    group by tx.#group_by#,
                                             tx.currency_id)
            select d.id                                                                                                                       as id,
                   d.currency_id                                                                                                              as currency_id,
                   d.date                                                                                                                     as date,
            
                   coalesce(sum(tx.amount) filter ( where type = 'DEPOSIT' and tx.deposit_status = 'COMPLETED' ), 0)
                       - coalesce(sum(tx.amount) filter ( where type = 'WITHDRAW' and tx.program_id is null ), 0)                             as total_deposited,
            
                   coalesce(sum(tx.amount) filter ( where type = 'TRANSFER' and tx.program_id is not null and tx.project_id is null ), 0)
                       - coalesce(sum(tx.amount) filter ( where type = 'REFUND' and tx.program_id is not null and tx.project_id is null ), 0) as total_allocated,
            
                   coalesce(sum(tx.amount) filter ( where type = 'TRANSFER' and tx.project_id is not null and tx.reward_id is null ), 0)
                       - coalesce(sum(tx.amount) filter ( where type = 'REFUND' and tx.project_id is not null and tx.reward_id is null ), 0)  as total_granted,
            
                   coalesce(sum(tx.amount) filter ( where type = 'TRANSFER' and tx.reward_id is not null and tx.payment_id is null ), 0)
                       - coalesce(sum(tx.amount) filter ( where type = 'REFUND' and tx.reward_id is not null and tx.payment_id is null ), 0)  as total_rewarded,
            
                   count(distinct tx.id)                                                                                                      as transaction_count
            from data d
                     left join accounting.all_transactions tx on date_trunc('month', tx.timestamp, 'UTC') = d.date and
                                                                 tx.type in ('DEPOSIT', 'WITHDRAW', 'TRANSFER', 'REFUND') and
                                                                 tx.currency_id = d.currency_id and
                                                                 tx.#group_by# = d.id and
                                                                 (coalesce(:types) is null or (
                                                                    ('DEPOSITED' in (:types) and tx.type = 'DEPOSIT' and tx.deposit_status != 'DRAFT') or
                                                                    ('ALLOCATED' in (:types) and tx.type = 'TRANSFER' and tx.program_id is not null and tx.project_id is null) or
                                                                    ('UNALLOCATED' in (:types) and tx.type = 'REFUND' and tx.program_id is not null and tx.project_id is null) or
                                                                    ('GRANTED' in (:types) and tx.type = 'TRANSFER' and tx.project_id is not null and tx.reward_id is null) or
                                                                    ('UNGRANTED' in (:types) and tx.type = 'REFUND' and tx.project_id is not null and tx.reward_id is null) or
                                                                    ('REWARDED' in (:types) and tx.type = 'TRANSFER' and tx.reward_id is not null and tx.payment_id is null)
                                                                 ))
                     left join sponsors s on s.id = tx.sponsor_id
                     left join programs pgm on pgm.id = tx.program_id
                     left join projects p on p.id = tx.project_id
                     left join rewards r on r.id = tx.reward_id
                     left join iam.all_users rr on rr.github_user_id = r.recipient_id
            where (cast(:search as text) is null or (concat(s.name, ' ', pgm.name, ' ', p.name, ' ', rr.login) ilike '%' || :search || '%'))
            group by d.id,
                     d.currency_id,
                     d.date
            order by d.date desc, d.id;
            """;

    public enum IdGrouping {
        SPONSOR_ID, PROGRAM_ID, PROJECT_ID
    }

    public List<BiFinancialMonthlyStatsReadEntity> findAll(UUID id,
                                                           IdGrouping groupBy,
                                                           ZonedDateTime fromDate,
                                                           ZonedDateTime toDate,
                                                           String search,
                                                           List<String> types) {
        final var idColumnPrefix = groupBy.name().toLowerCase();
        final var query = entityManager.createNativeQuery(SELECT_QUERY.replaceAll("#group_by#", idColumnPrefix), BiFinancialMonthlyStatsReadEntity.class);
        query.setParameter("id", id);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("search", search);
        query.setParameter("types", types);
        return query.getResultList();
    }
}
