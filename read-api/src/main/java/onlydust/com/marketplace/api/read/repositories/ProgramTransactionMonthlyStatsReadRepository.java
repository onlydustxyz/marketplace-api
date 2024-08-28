package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionMonthlyStatReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface ProgramTransactionMonthlyStatsReadRepository extends Repository<ProgramTransactionMonthlyStatReadEntity,
        ProgramTransactionMonthlyStatReadEntity.PrimaryKey> {
    @Query(value = """
            with data AS (SELECT generate_series(date_trunc('month', coalesce(cast(:fromDate as timestamp), min(abt.timestamp), CURRENT_DATE)),
                                                 date_trunc('month', coalesce(cast(:toDate as timestamp), CURRENT_DATE)),
                                                 '1 mon') AS date,
                                 abt.program_id as program_id,
                                 abt.currency_id as currency_id
                          FROM accounting.account_book_transactions abt
                          WHERE abt.program_id = :programId
                          GROUP BY abt.program_id,
                                   abt.currency_id),
            stats as (select abt.program_id                                                                                                                              as program_id,
                             abt.currency_id                                                                                                                             as currency_id,
                             date_trunc('month', abt.timestamp)                                                                                                          as date,
            
                             coalesce(sum(amount) filter ( where type in ('MINT', 'TRANSFER') and project_id is null ), 0)
                                 - coalesce(sum(amount) filter ( where type in ('REFUND', 'BURN') and project_id is null ), 0)                                           as total_received,
            
                             coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is null ), 0)
                                 - coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is null ), 0)                            as total_granted,
            
                             coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is not null and payment_id is null ), 0)
                                 - coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is not null and payment_id is null ), 0) as total_rewarded,
            
                             count(*)                                                                                                                                    as transaction_count
                           from accounting.account_book_transactions abt
                                    left join projects p on p.id = abt.project_id
                                    join programs pgm on pgm.id = abt.program_id
                           where (cast(:search as text) is null or p.name ilike '%' || :search || '%' or pgm.name ilike '%' || :search || '%')
                             and (coalesce(:types) is null or (
                               ('GRANTED' in (:types) and abt.type = 'TRANSFER' and abt.project_id is not null and abt.reward_id is null) or
                               ('RECEIVED' in (:types) and abt.type = 'TRANSFER' and abt.project_id is null) or
                               ('RETURNED' in (:types) and abt.type = 'REFUND' and abt.project_id is null)
                               ))
                           group by abt.program_id,
                                    abt.currency_id,
                                    date_trunc('month', abt.timestamp))
            select d.program_id                                                    as program_id,
                   d.currency_id                                                   as currency_id,
                   d.date                                                          as date,
                   sum(coalesce(s.total_received, 0) - coalesce(s.total_granted, 0))
                   over (partition by d.program_id, d.currency_id order by d.date) as total_available,
                   coalesce(s.total_granted, 0)                                    as total_granted,
                   coalesce(s.total_rewarded, 0)                                   as total_rewarded,
                   coalesce(s.transaction_count, 0)                                as transaction_count
            from data d
                   left join stats s on d.program_id = s.program_id and d.currency_id = s.currency_id and d.date = s.date
            """, nativeQuery = true)
    List<ProgramTransactionMonthlyStatReadEntity> findAll(final UUID programId, Date fromDate, Date toDate, String search, List<String> types);
}
