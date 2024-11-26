package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.api.postgres.adapter.PostgresBiProjectorAdapter;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.model.Application;
import onlydust.com.marketplace.project.domain.model.GithubComment;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.port.output.ProjectApplicationStoragePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationHelper {
    @Autowired
    private ProjectApplicationStoragePort projectApplicationStoragePort;
    @Autowired
    private PostgresBiProjectorAdapter postgresBiProjectorAdapter;

    private final Faker faker = new Faker();

    public Application create(ProjectId projectId, GithubIssue.Id issueId, GithubUserId applicantId) {
        final var application = Application.fromMarketplace(
                projectId,
                applicantId.value(),
                issueId
        );
        application.commentId(GithubComment.Id.random());
        application.commentBody(faker.lorem().sentence());
        projectApplicationStoragePort.save(application);
        postgresBiProjectorAdapter.onApplicationCreated(application);
        return application;
    }
}
