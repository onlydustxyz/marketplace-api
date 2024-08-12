package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.program.ProgramTransactionStatReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface ProgramTransactionStatsReadRepository extends Repository<ProgramTransactionStatReadEntity, ProgramTransactionStatReadEntity.PrimaryKey> {
    @Query(value = """
            select
                1 as index,
                sa.sponsor_id as program_id,
                ab.currency_id as currency_id,
                coalesce(sum(abt.amount) filter ( where abt.type in ('MINT', 'TRANSFER') and abt.project_id is null ), 0)
                    - coalesce(sum(abt.amount) filter ( where abt.type in ('REFUND', 'BURN') and abt.project_id is null ), 0)
                    - coalesce(sum(abt.amount) filter ( where abt.type = 'TRANSFER' and abt.project_id is not null and abt.reward_id is null ), 0)
                    + coalesce(sum(abt.amount) filter ( where abt.type = 'REFUND' and abt.project_id is not null and abt.reward_id is null ), 0)
                    as total_available,
                coalesce(sum(abt.amount) filter ( where abt.type = 'TRANSFER' and abt.project_id is not null and abt.reward_id is null ), 0)
                    - coalesce(sum(abt.amount) filter ( where abt.type = 'REFUND' and abt.project_id is not null and abt.reward_id is null ), 0)
                    as total_granted,
                coalesce(sum(abt.amount) filter ( where abt.type = 'TRANSFER' and abt.project_id is not null and abt.reward_id is not null and abt.payment_id is null ), 0)
                    - coalesce(sum(abt.amount) filter ( where abt.type = 'REFUND' and abt.project_id is not null and abt.reward_id is not null and abt.payment_id is null ), 0)
                    as total_rewarded
            from
                accounting.account_book_transactions abt
            join accounting.sponsor_accounts sa on sa.id = abt.sponsor_account_id
            join accounting.account_books ab on ab.id = abt.account_book_id
            where
                sa.sponsor_id = :programId
            group by
                sa.sponsor_id,
                ab.currency_id
            """, nativeQuery = true)
    List<ProgramTransactionStatReadEntity> findAll(final UUID programId);
}
