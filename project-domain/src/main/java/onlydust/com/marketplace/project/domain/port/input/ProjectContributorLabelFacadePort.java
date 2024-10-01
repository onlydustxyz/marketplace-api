package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;

public interface ProjectContributorLabelFacadePort {

    ProjectContributorLabel createLabel(final @NonNull UserId projectLeadId, final @NonNull ProjectId projectId, final @NonNull String name);

    void deleteLabel(final @NonNull UserId projectLeadId, final @NonNull ProjectContributorLabel.Id labelId);

    void updateLabel(final @NonNull UserId projectLeadId, final @NonNull ProjectContributorLabel.Id labelId, final @NonNull String name);
}
