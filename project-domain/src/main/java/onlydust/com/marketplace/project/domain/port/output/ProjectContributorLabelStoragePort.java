package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.ProjectContributorLabel;

public interface ProjectContributorLabelStoragePort {

    void save(ProjectContributorLabel projectContributorLabel);
}
