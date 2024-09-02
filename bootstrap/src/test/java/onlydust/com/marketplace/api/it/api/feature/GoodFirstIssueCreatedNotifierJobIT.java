package onlydust.com.marketplace.api.it.api.feature;

import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.project.domain.job.GoodFirstIssueCreatedNotifierJob;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.model.Project;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.service.NotificationSettingsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class GoodFirstIssueCreatedNotifierJobIT extends AbstractMarketplaceApiIT {

    @Autowired
    NotificationSettingsService notificationSettingsService;
    @Autowired
    GoodFirstIssueCreatedNotifierJob goodFirstIssueCreatedNotifierJob;
    @Autowired
    CustomerIOProperties customerIOProperties;
    @Autowired
    ProjectStoragePort projectStoragePort;
    @Autowired
    GithubStoragePort githubStoragePort;

    @Test
    void should_notify_users_on_new_good_first_issues() throws InterruptedException {
        // Given
        final UserAuthHelper.AuthenticatedUser user1 = userAuthHelper.create();
        final NotificationRecipient.Id recipientId1 = NotificationRecipient.Id.of(user1.user().getId());
        final UserAuthHelper.AuthenticatedUser user2 = userAuthHelper.create();
        final NotificationRecipient.Id recipientId2 = NotificationRecipient.Id.of(user2.user().getId());
        final UserAuthHelper.AuthenticatedUser dummyContributor = userAuthHelper.create();
        final ProjectId projectId1 = projectHelper.create(user1, faker.rickAndMorty().character());
        final ProjectId projectId2 = projectHelper.create(user2, faker.lordOfTheRings().character());
        final GithubRepo repo1 = githubHelper.createRepo();
        final GithubRepo repo2 = githubHelper.createRepo();
        projectHelper.addRepo(projectId1, repo1.getId());
        projectHelper.addRepo(projectId2, repo2.getId());
        final Long issueId11 = githubHelper.createIssue(repo1.getId(), ZonedDateTime.now(), null, "OPEN", dummyContributor);
        githubHelper.createIssue(repo1.getId(), ZonedDateTime.now(), null, "OPEN", dummyContributor);
        final Long issueId13 = githubHelper.createIssue(repo1.getId(), ZonedDateTime.now(), null, "COMPLETED", dummyContributor);
        final Long issueId14 = githubHelper.createIssue(repo1.getId(), ZonedDateTime.now().minusMinutes(6), null, "OPEN", dummyContributor);
        final Long issueId15 = githubHelper.createIssue(repo1.getId(), ZonedDateTime.now(), null, "OPEN", dummyContributor);
        final Long issueId16= githubHelper.createIssue(repo1.getId(), ZonedDateTime.now(), null, "OPEN", dummyContributor);
        githubHelper.addLabelToIssue(issueId11, "good-First_ISSUE", ZonedDateTime.now());
        githubHelper.addLabelToIssue(issueId13, "good-First_ISSUE", ZonedDateTime.now());
        githubHelper.addLabelToIssue(issueId14, "good-First_ISSUE2", ZonedDateTime.now().minusMinutes(6));
        githubHelper.addLabelToIssue(issueId11, "JavaLover", ZonedDateTime.now());
        githubHelper.addLabelToIssue(issueId15, "good-first-issue-10", ZonedDateTime.now());
        githubHelper.assignIssueToContributor(issueId15, dummyContributor.user().getGithubUserId());
        githubHelper.addLabelToIssue(issueId16, "good-first-issue-20", ZonedDateTime.now());
        githubHelper.addLabelToIssue(issueId16, "hackathon-gfi", ZonedDateTime.now());
        hackathonHelper.createHackathon(Hackathon.Status.PUBLISHED, List.of("hackathon-gfi"), List.of(projectId1));
        final Long issueId21 = githubHelper.createIssue(repo2.getId(), ZonedDateTime.now().minusDays(1), null, "OPEN", dummyContributor);
        githubHelper.addLabelToIssue(issueId21, "GOOD-First_ISSUE", ZonedDateTime.now());
        notificationSettingsService.patchNotificationSettingsForProject(recipientId1, new NotificationSettings.Project(projectId1, Optional.of(false)));
        notificationSettingsService.patchNotificationSettingsForProject(recipientId1, new NotificationSettings.Project(projectId2, Optional.of(true)));
        notificationSettingsService.patchNotificationSettingsForProject(recipientId2, new NotificationSettings.Project(projectId2, Optional.of(false)));
        notificationSettingsService.patchNotificationSettingsForProject(recipientId2, new NotificationSettings.Project(projectId1, Optional.of(true)));

        // When
        goodFirstIssueCreatedNotifierJob.run();
        Thread.sleep(1000L);

        // Then
        final Project project1 = projectStoragePort.getById(projectId1.value()).orElseThrow();
        final Project project2 = projectStoragePort.getById(projectId2.value()).orElseThrow();
        final GithubIssue issue11 = githubStoragePort.findIssueById(GithubIssue.Id.of(issueId11)).orElseThrow();
        final GithubIssue issue21 = githubStoragePort.findIssueById(GithubIssue.Id.of(issueId21)).orElseThrow();


        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id", equalTo(customerIOProperties.getIssueCreatedEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(user2.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(user2.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("New good first issue")))
                        .withRequestBody(matchingJsonPath("$.message_data.description", equalTo(("We are excited to inform you that a new issue has been " +
                                                                                                 "posted on the project <b>%s</b> you subscribed to." +
                                                                                                 " You can view the details of the issue by clicking the link" +
                                                                                                 " below.").formatted(project1.getName()))))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("View issue")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link",
                                equalTo("https://develop-app.onlydust.com/p/%s".formatted(project1.getSlug()))))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.title", equalTo(issue11.title())))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.createdAt", equalTo("Today")))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.repository", equalTo(issue11.repoName())))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.tags", equalToJson("[\"JavaLover\", \"good-First_ISSUE\"]")))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.createdBy.name", equalTo(issue11.authorLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.createdBy.avatarUrl", equalTo(issue11.authorAvatarUrl())))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(user2.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("New good first issue"))));

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(matchingJsonPath("$.transactional_message_id", equalTo(customerIOProperties.getIssueCreatedEmailId().toString())))
                        .withRequestBody(matchingJsonPath("$.identifiers.id", equalTo(user1.user().getId().toString())))
                        .withRequestBody(matchingJsonPath("$.message_data.username", equalTo(user1.user().getGithubLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.title", equalTo("New good first issue")))
                        .withRequestBody(matchingJsonPath("$.message_data.description", equalTo(("We are excited to inform you that a new issue has been " +
                                                                                                 "posted on the project <b>%s</b> you subscribed to." +
                                                                                                 " You can view the details of the issue by clicking the link" +
                                                                                                 " below.").formatted(project2.getName()))))
                        .withRequestBody(matchingJsonPath("$.message_data.button.text", equalTo("View issue")))
                        .withRequestBody(matchingJsonPath("$.message_data.button.link",
                                equalTo("https://develop-app.onlydust.com/p/%s".formatted(project2.getSlug()))))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.title", equalTo(issue21.title())))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.createdAt", equalTo("Today")))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.repository", equalTo(issue21.repoName())))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.tags", equalToJson("[\"GOOD-First_ISSUE\"]")))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.createdBy.name", equalTo(issue21.authorLogin())))
                        .withRequestBody(matchingJsonPath("$.message_data.issue.createdBy.avatarUrl", equalTo(issue21.authorAvatarUrl())))
                        .withRequestBody(matchingJsonPath("$.to", equalTo(user1.user().getEmail())))
                        .withRequestBody(matchingJsonPath("$.subject", equalTo("New good first issue"))));

        customerIOWireMockServer.verify(2, postRequestedFor(urlEqualTo("/send/email")));
    }
}
