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
            with stats as (select abt.sponsor_id                                                                                                    as sponsor_id,
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
            
                                  count(*)                                                                                                          as transaction_count
                           from accounting.account_book_transactions abt
                                    left join programs pgm on pgm.id = abt.program_id
                           where abt.sponsor_id = :sponsorId
                             and (cast(:fromDate as text) is null or abt.timestamp >= :fromDate)
                             and (cast(:toDate as text) is null or date_trunc('month', abt.timestamp) <= :toDate)
                             and (cast(:search as text) is null or (pgm.name ilike '%' || :search || '%' and project_id is null))
                             and (coalesce(:types) is null or (
                               ('DEPOSITED' in (:types) and abt.type = 'MINT' and abt.program_id is null) or
                               ('ALLOCATED' in (:types) and abt.type = 'TRANSFER' and abt.program_id is not null and project_id is null) or
                               ('RETURNED'  in (:types) and abt.type = 'REFUND' and abt.program_id is not null and project_id is null)
                               ))
                           group by abt.sponsor_id,
                                    abt.currency_id,
                                    date_trunc('month', abt.timestamp))
            select s.sponsor_id                                                    as sponsor_id,
                   s.currency_id                                                   as currency_id,
                   s.date                                                          as date,
                   sum(s.total_deposited - s.total_allocated)
                   over (partition by s.sponsor_id, s.currency_id order by s.date) as total_available,
                   s.total_allocated                                               as total_allocated,
                   s.total_granted                                                 as total_granted,
                   s.total_rewarded                                                as total_rewarded,
                   s.transaction_count                                             as transaction_count
            from stats s
            """, nativeQuery = true)
    List<SponsorTransactionMonthlyStatReadEntity> findAll(final UUID sponsorId, Date fromDate, Date toDate, String search, List<String> types);
}
