package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionMonthlyStatReadEntity;
import onlydust.com.marketplace.api.read.entities.program.SponsorTransactionMonthlyStatReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface SponsorTransactionMonthlyStatsReadRepository extends Repository<SponsorTransactionMonthlyStatReadEntity,
        ProgramTransactionMonthlyStatReadEntity.PrimaryKey> {
    @Query(value = """
            with data AS (SELECT generate_series(date_trunc('month', coalesce(cast(:fromDate as timestamp), min(abt.timestamp), CURRENT_DATE)),
                                                 date_trunc('month', coalesce(cast(:toDate as timestamp), CURRENT_DATE)),
                                                 '1 mon') AS date,
                                 abt.sponsor_id  as sponsor_id,
                                 abt.currency_id as currency_id
                          FROM accounting.account_book_transactions abt
                          WHERE abt.sponsor_id = :sponsorId
                          GROUP BY abt.sponsor_id,
                                   abt.currency_id),
                stats as (select abt.sponsor_id                                                                                                    as sponsor_id,
                                  abt.currency_id                                                                                                   as currency_id,
                                  date_trunc('month', abt.timestamp)                                                                                as date,
            
                                  coalesce(sum(amount) filter ( where type = 'MINT' and program_id is null ), 0)
                                      - coalesce(sum(amount) filter ( where type = 'REFUND' and program_id is null ), 0)                            as total_deposited,
            
                                  coalesce(sum(amount) filter ( where type = 'TRANSFER' and program_id is not null and project_id is null ), 0)
                                      - coalesce(sum(amount) filter ( where type = 'REFUND' and program_id is not null and project_id is null ), 0) as total_allocated,
            
                                  coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is null ), 0)
                                      - coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is null ), 0)  as total_granted,
            
                                  coalesce(sum(amount) filter ( where type = 'TRANSFER' and reward_id is not null and payment_id is null ), 0)
                                      - coalesce(sum(amount) filter ( where type = 'REFUND' and reward_id is not null and payment_id is null ), 0)  as total_rewarded,
            
                                  count(*) filter ( where abt.project_id is null )                                                                  as transaction_count
                           from accounting.account_book_transactions abt
                                    left join programs pgm on pgm.id = abt.program_id
                           where (cast(:search as text) is null or (pgm.name ilike '%' || :search || '%' and project_id is null))
                             and (coalesce(:types) is null or (
                               ('DEPOSITED' in (:types) and abt.type = 'MINT' and abt.program_id is null) or
                               ('ALLOCATED' in (:types) and abt.type = 'TRANSFER' and abt.program_id is not null and project_id is null) or
                               ('RETURNED'  in (:types) and abt.type = 'REFUND' and abt.program_id is not null and project_id is null)
                               ))
                           group by abt.sponsor_id,
                                    abt.currency_id,
                                    date_trunc('month', abt.timestamp))
            select d.sponsor_id                                                    as sponsor_id,
                   d.currency_id                                                   as currency_id,
                   d.date                                                          as date,
                   sum(coalesce(s.total_deposited, 0) - coalesce(s.total_allocated, 0))
                   over (partition by d.sponsor_id, d.currency_id order by d.date) as total_available,
                   coalesce(s.total_allocated, 0)                                  as total_allocated,
                   coalesce(s.total_granted, 0)                                    as total_granted,
                   coalesce(s.total_rewarded, 0)                                   as total_rewarded,
                   coalesce(s.transaction_count, 0)                                as transaction_count
            from data d
                   left join stats s on d.sponsor_id = s.sponsor_id and d.currency_id = s.currency_id and d.date = s.date
            """, nativeQuery = true)
    List<SponsorTransactionMonthlyStatReadEntity> findAll(final UUID sponsorId, Date fromDate, Date toDate, String search, List<String> types);
}
