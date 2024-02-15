package onlydust.com.marketplace.api.domain.view;

import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.pagination.Page;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ProjectContributorsLinkViewPage extends Page<ProjectContributorsLinkView> {
    private final Boolean hasHiddenContributors;

    public Boolean hasHiddenContributors() {
        return Boolean.TRUE.equals(hasHiddenContributors);
    }
}
