package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;

import java.util.List;
import java.util.Optional;

public interface ProjectApplicationStoragePort {

    boolean saveNew(@NonNull Application application);

    void save(@NonNull Application... applications);

    Optional<Application> findApplication(Application.Id id);

    Optional<Application> findApplication(Long applicantId, ProjectId projectId, GithubIssue.Id issueId);

    List<Application> findApplications(Long applicantId, GithubIssue.Id issueId);

    List<Application> findApplications(GithubComment.Id commentId);

    List<Application> findApplications(GithubIssue.Id issueId);

    void deleteApplications(Application.Id... applicationIds);

    void deleteApplicationsByIssueId(GithubIssue.Id issueId);

    void deleteObsoleteApplications();

    List<Application> findApplicationsOnIssueAndProject(GithubIssue.Id issueId, ProjectId projectId);
}
