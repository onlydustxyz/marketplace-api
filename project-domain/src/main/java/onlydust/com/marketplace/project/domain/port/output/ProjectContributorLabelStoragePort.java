package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;

import java.util.Optional;

public interface ProjectContributorLabelStoragePort {

    void save(@NonNull ProjectContributorLabel projectContributorLabel);

    void delete(@NonNull ProjectContributorLabel.Id labelId);

    Optional<ProjectContributorLabel> get(@NonNull ProjectContributorLabel.Id labelId);
}
