package onlydust.com.marketplace.api.postgres.adapter.repository;

import onlydust.com.marketplace.api.domain.view.ProjectRewardView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectRewardViewEntity;

import java.util.List;
import java.util.UUID;

public class CustomProjectRewardRepository {
    public Integer getCount(UUID projectId) {
        return null;
    }

    public List<ProjectRewardViewEntity> getViewEntities(UUID projectId, ProjectRewardView.SortBy sortBy, int pageIndex, int pageSize) {
        return null;
    }
}
