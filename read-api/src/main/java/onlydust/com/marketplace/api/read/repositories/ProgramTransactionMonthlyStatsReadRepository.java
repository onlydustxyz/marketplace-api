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
            with stats as (
                select
                    sa.sponsor_id                                                                                                                               as program_id,
                    ab.currency_id                                                                                                                              as currency_id,
                    date_trunc('month', abt.timestamp)                                                                                                          as date,
            
                    coalesce(sum(amount) filter ( where type in ('MINT', 'TRANSFER') and project_id is null ), 0)
                        - coalesce(sum(amount) filter ( where type in ('REFUND', 'BURN') and project_id is null ), 0)
                        - coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is null ), 0)
                        + coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is null ), 0)                            as total_available,
            
                    coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is null ), 0)
                        - coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is null ), 0)                            as total_granted,
            
                    coalesce(sum(amount) filter ( where type = 'TRANSFER' and project_id is not null and reward_id is not null and payment_id is null ), 0)
                        - coalesce(sum(amount) filter ( where type = 'REFUND' and project_id is not null and reward_id is not null and payment_id is null ), 0) as total_rewarded
                from
                    accounting.account_book_transactions abt
                join accounting.sponsor_accounts sa on sa.id = abt.sponsor_account_id
                join accounting.account_books ab on ab.id = abt.account_book_id
                join sponsors s on s.id = sa.sponsor_id
                left join projects p on p.id = abt.project_id
                where
                    sa.sponsor_id = :programId and
                    (cast(:fromDate as text) is null or abt.timestamp >= :fromDate) and
                    (cast(:toDate as text) is null or date_trunc('month', abt.timestamp) <= :toDate) and
                    (cast(:search as text) is null or p.name ilike '%' || :search || '%' or s.name ilike '%' || :search || '%') and
                    (coalesce(:types) is null or (
                        'GRANTED' in (:types) and abt.type in ('TRANSFER', 'REFUND') and abt.project_id is not null and abt.reward_id is null or
                        'RECEIVED' in (:types) and abt.type in ('MINT', 'TRANSFER') and abt.project_id is null or
                        'RETURNED' in (:types) and abt.type in ('REFUND', 'BURN') and abt.project_id is null
                    ))
                group by
                    sa.sponsor_id,
                    ab.currency_id,
                    date_trunc('month', abt.timestamp)
            )
            select
                s.program_id as                                                                             program_id,
                s.currency_id as                                                                            currency_id,
                s.date as                                                                                   date,
                sum(s.total_available) over (partition by s.program_id, s.currency_id order by s.date) as   total_available,
                s.total_granted as                                                                          total_granted,
                s.total_rewarded as                                                                         total_rewarded
            from
                stats s
            """, nativeQuery = true)
    List<ProgramTransactionMonthlyStatReadEntity> findAll(final UUID programId, Date fromDate, Date toDate, String search, List<String> types);
}
