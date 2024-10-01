package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;

public interface ProjectContributorLabelFacadePort {

    ProjectContributorLabel createContributorLabel(final @NonNull ProjectId projectId, final @NonNull String name);
}
