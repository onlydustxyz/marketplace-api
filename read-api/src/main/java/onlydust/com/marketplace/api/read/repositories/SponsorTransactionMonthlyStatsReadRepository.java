package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionMonthlyStatReadEntity;
import onlydust.com.marketplace.api.read.entities.program.SponsorTransactionMonthlyStatReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface SponsorTransactionMonthlyStatsReadRepository extends Repository<SponsorTransactionMonthlyStatReadEntity,
        ProgramTransactionMonthlyStatReadEntity.PrimaryKey> {
    @Query(value = """
            with data AS (SELECT generate_series(date_trunc('month', coalesce(cast(:fromDate as timestamptz), min(tx.timestamp), now()), 'UTC'),
                                                 date_trunc('month', coalesce(cast(:toDate as timestamptz), now()), 'UTC'),
                                                 '1 mon') AS date,
                                 tx.sponsor_id  as sponsor_id,
                                 tx.currency_id as currency_id
                          FROM accounting.all_transactions tx
                          WHERE tx.sponsor_id = :sponsorId
                          GROUP BY tx.sponsor_id,
                                   tx.currency_id),
                stats as (select tx.sponsor_id                                                                                                     as sponsor_id,
                                  tx.currency_id                                                                                                   as currency_id,
                                  date_trunc('month', tx.timestamp, 'UTC')                                                                                as date,
            
                                  coalesce(sum(amount) filter ( where type = 'DEPOSIT' and tx.deposit_status = 'COMPLETED' ), 0)
                                      - coalesce(sum(amount) filter ( where type = 'WITHDRAW' and program_id is null ), 0)                          as total_deposited,
            
                                  coalesce(sum(amount) filter ( where type = 'TRANSFER' and program_id is not null and project_id is null ), 0)
                                      - coalesce(sum(amount) filter ( where type = 'REFUND' and program_id is not null and project_id is null ), 0) as total_allocated,
            
                                  coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is null ), 0)
                                      - coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is null ), 0)  as total_granted,
            
                                  coalesce(sum(amount) filter ( where type = 'TRANSFER' and reward_id is not null and payment_id is null ), 0)
                                      - coalesce(sum(amount) filter ( where type = 'REFUND' and reward_id is not null and payment_id is null ), 0)  as total_rewarded,
            
                                  count(*) filter ( where abt.project_id is null )                                                                  as transaction_count
                           from accounting.all_transactions tx
                                    left join programs pgm on pgm.id = tx.program_id
                           where (cast(:search as text) is null or (pgm.name ilike '%' || :search || '%' and project_id is null))
                             and (coalesce(:types) is null or (
                               ('DEPOSITED' in (:types) and tx.type = 'DEPOSIT' and tx.deposit_status = 'COMPLETED') or
                               ('ALLOCATED' in (:types) and tx.type = 'TRANSFER' and tx.program_id is not null and project_id is null) or
                               ('RETURNED'  in (:types) and tx.type = 'REFUND' and tx.program_id is not null and project_id is null)
                               ))
                           group by tx.sponsor_id,
                                    tx.currency_id,
                                    date_trunc('month', tx.timestamp, 'UTC'))
            select d.sponsor_id                                                    as sponsor_id,
                   d.currency_id                                                   as currency_id,
                   cast(d.date as timestamptz)                                     as date,
                   sum(coalesce(s.total_deposited, 0) - coalesce(s.total_allocated, 0))
                   over (partition by d.sponsor_id, d.currency_id order by d.date) as total_available,
                   coalesce(s.total_allocated, 0)                                  as total_allocated,
                   coalesce(s.total_granted, 0)                                    as total_granted,
                   coalesce(s.total_rewarded, 0)                                   as total_rewarded,
                   coalesce(s.transaction_count, 0)                                as transaction_count
            from data d
                   left join stats s on d.sponsor_id = s.sponsor_id and d.currency_id = s.currency_id and d.date = s.date
            """, nativeQuery = true)
    List<SponsorTransactionMonthlyStatReadEntity> findAll(final UUID sponsorId,
                                                          ZonedDateTime fromDate,
                                                          ZonedDateTime toDate,
                                                          String search,
                                                          List<String> types);
}
