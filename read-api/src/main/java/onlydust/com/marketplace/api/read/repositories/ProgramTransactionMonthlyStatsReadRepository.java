package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionMonthlyStatReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface ProgramTransactionMonthlyStatsReadRepository extends Repository<ProgramTransactionMonthlyStatReadEntity,
        ProgramTransactionMonthlyStatReadEntity.PrimaryKey> {
    @Query(value = """
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
            where
                sa.sponsor_id = :programId
            group by
                sa.sponsor_id,
                ab.currency_id,
                date_trunc('month', abt.timestamp)
            """, nativeQuery = true)
    List<ProgramTransactionMonthlyStatReadEntity> findAll(final UUID programId);
}
