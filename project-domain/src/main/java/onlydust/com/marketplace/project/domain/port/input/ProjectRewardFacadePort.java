package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.SortDirection;
import onlydust.com.marketplace.project.domain.view.*;

import java.util.UUID;

public interface ProjectRewardFacadePort {

    ProjectRewardsPageView getRewards(UUID projectId, UUID projectLeadId,
                                      ProjectRewardView.Filters filters,
                                      Integer pageIndex, Integer pageSize,
                                      ProjectRewardView.SortBy sortBy, SortDirection sortDirection);

    ProjectBudgetsView getBudgets(UUID projectId, UUID projectLeadId);

    RewardDetailsView getRewardByIdForProjectLead(UUID projectId, UUID rewardId, UUID userId);

    Page<RewardItemView> getRewardItemsPageByIdForProjectLead(UUID projectId, UUID rewardId, UUID projectLead,
                                                              int pageIndex, int pageSize);

}
