package onlydust.com.marketplace.api.read.repositories;

import onlydust.com.marketplace.api.read.entities.program.ProgramBudgetReadEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

public interface ProgramBudgetReadRepository extends Repository<ProgramBudgetReadEntity, ProgramBudgetReadEntity.PrimaryKey> {
    @Query(value = """
            SELECT
                s.id as program_id,
                abt.currency_id as currency_id,
                sum(abt.amount) filter ( where abt.project_id is null )
                    - sum(abt.amount) filter ( where abt.project_id is not null and abt.reward_id is null ) as amount
            FROM
                sponsors s
            JOIN accounting.sponsor_accounts sa ON s.id = sa.sponsor_id
            JOIN accounting.account_book_transactions abt ON sa.id = abt.sponsor_account_id
            WHERE
                s.id = :programId
            GROUP BY
                s.id,
                abt.currency_id
            """, nativeQuery = true)
    List<ProgramBudgetReadEntity> getTotalAvailable(final UUID programId);

    @Query(value = """
            SELECT
                s.id as program_id,
                abt.currency_id as currency_id,
                sum(abt.amount) filter ( where abt.project_id is not null and abt.reward_id is null ) as amount
            FROM
                sponsors s
            JOIN accounting.sponsor_accounts sa ON s.id = sa.sponsor_id
            JOIN accounting.account_book_transactions abt ON sa.id = abt.sponsor_account_id
            WHERE
                s.id = :programId
            GROUP BY
                s.id,
                abt.currency_id
            """, nativeQuery = true)
    List<ProgramBudgetReadEntity> getTotalGranted(final UUID programId);

    @Query(value = """
            SELECT
                s.id as program_id,
                abt.currency_id as currency_id,
                sum(abt.amount) filter ( where abt.project_id is not null and abt.reward_id is not null and abt.payment_id is null ) as amount
            FROM
                sponsors s
            JOIN accounting.sponsor_accounts sa ON s.id = sa.sponsor_id
            JOIN accounting.account_book_transactions abt ON sa.id = abt.sponsor_account_id
            WHERE
                s.id = :programId
            GROUP BY
                s.id,
                abt.currency_id
            """, nativeQuery = true)
    List<ProgramBudgetReadEntity> getTotalRewarded(final UUID programId);
}
