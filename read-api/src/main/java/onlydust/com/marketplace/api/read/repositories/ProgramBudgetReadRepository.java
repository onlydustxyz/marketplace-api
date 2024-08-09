package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.program.ProgramBudgetReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface ProgramBudgetReadRepository extends Repository<ProgramBudgetReadEntity, ProgramBudgetReadEntity.PrimaryKey> {
    @Query(value = """
            select
                1 as index,
                s.id as program_id,
                abt.currency_id as currency_id,
                sum(abt.amount) filter ( where abt.project_id is null )
                    - sum(abt.amount) filter ( where abt.project_id is not null and abt.reward_id is null ) as amount
            from
                sponsors s
            join accounting.sponsor_accounts sa on s.id = sa.sponsor_id
            join accounting.account_book_transactions abt on sa.id = abt.sponsor_account_id
            where
                s.id = :programId
            group by
                s.id,
                abt.currency_id
            """, nativeQuery = true)
    List<ProgramBudgetReadEntity> getTotalAvailable(final UUID programId);

    @Query(value = """
            select
                2 as index,
                s.id as program_id,
                abt.currency_id as currency_id,
                sum(abt.amount) as amount
            from
                sponsors s
            join accounting.sponsor_accounts sa on s.id = sa.sponsor_id
            join accounting.account_book_transactions abt on sa.id = abt.sponsor_account_id
            where
                s.id = :programId and
                abt.project_id is not null and
                abt.reward_id is null
            group by
                s.id,
                abt.currency_id
            """, nativeQuery = true)
    List<ProgramBudgetReadEntity> getTotalGranted(final UUID programId);

    @Query(value = """
            select
                3 as index,
                s.id as program_id,
                abt.currency_id as currency_id,
                sum(abt.amount) as amount
            from
                sponsors s
            join accounting.sponsor_accounts sa on s.id = sa.sponsor_id
            join accounting.account_book_transactions abt on sa.id = abt.sponsor_account_id
            where
                s.id = :programId and
                abt.project_id is not null and
                abt.reward_id is not null and
                abt.payment_id is null
            group by
                s.id,
                abt.currency_id
            """, nativeQuery = true)
    List<ProgramBudgetReadEntity> getTotalRewarded(final UUID programId);
}
