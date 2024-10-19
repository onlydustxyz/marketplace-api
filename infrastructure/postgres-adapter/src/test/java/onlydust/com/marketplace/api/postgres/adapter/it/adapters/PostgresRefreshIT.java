package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.api.postgres.adapter.PostgresBiProjectorAdapter;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.kernel.model.*;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PostgresRefreshIT extends AbstractPostgresIT {
    @Autowired
    private ProjectStoragePort projectStoragePort;
    @Autowired
    private UserStoragePort userStoragePort;
    @Autowired
    private PostgresBiProjectorAdapter postgresBiProjectorAdapter;

    @Test
    @Transactional
    void should_be_able_to_refresh_materialized_views() {
        assertDoesNotThrow(() -> userStoragePort.refreshUserRanksAndStats());
        assertDoesNotThrow(() -> projectStoragePort.refreshRecommendations());
        assertDoesNotThrow(() -> projectStoragePort.refreshStats());
        assertDoesNotThrow(() -> postgresBiProjectorAdapter.onRewardCreated(RewardId.random(), null));
        assertDoesNotThrow(() -> postgresBiProjectorAdapter.onRewardCancelled(RewardId.random()));
        assertDoesNotThrow(() -> postgresBiProjectorAdapter.onRewardPaid(RewardId.random()));
        assertDoesNotThrow(() -> postgresBiProjectorAdapter.onFundsGrantedToProject(ProgramId.random(), ProjectId.random(), PositiveAmount.of(1000L),
                Currency.Id.random()));
        assertDoesNotThrow(() -> postgresBiProjectorAdapter.onFundsRefundedByProject(ProjectId.random(), ProgramId.random(), PositiveAmount.of(1000L),
                Currency.Id.random()));
        assertDoesNotThrow(() -> postgresBiProjectorAdapter.onContributionsChanged(123L, ContributionUUID.of(456L)));
        assertDoesNotThrow(() -> postgresBiProjectorAdapter.onLinkedReposChanged(ProjectId.random(), Set.of(123L), Set.of(456L)));
        assertDoesNotThrow(() -> postgresBiProjectorAdapter.onProjectCreated(ProjectId.random(), UserId.random()));
        assertDoesNotThrow(() -> postgresBiProjectorAdapter.onUserSignedUp(AuthenticatedUser.builder().githubUserId(123L).build()));

    }
}
