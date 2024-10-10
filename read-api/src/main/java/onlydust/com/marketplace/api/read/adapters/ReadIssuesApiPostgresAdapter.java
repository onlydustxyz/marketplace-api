package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadIssuesApi;
import onlydust.com.marketplace.api.contract.model.ApplicantsQueryParams;
import onlydust.com.marketplace.api.contract.model.GithubIssueResponse;
import onlydust.com.marketplace.api.contract.model.IssueApplicantsPageResponse;
import onlydust.com.marketplace.api.read.entities.project.ApplicationReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectLinkReadEntity;
import onlydust.com.marketplace.api.read.repositories.ApplicationReadRepository;
import onlydust.com.marketplace.api.read.repositories.ContributorKpisReadRepository;
import onlydust.com.marketplace.api.read.repositories.GithubIssueReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadIssuesApiPostgresAdapter implements ReadIssuesApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final GithubIssueReadRepository githubIssueReadRepository;
    private final ApplicationReadRepository applicationReadRepository;
    private final ContributorKpisReadRepository contributorKpisReadRepository;

    @Override
    public ResponseEntity<GithubIssueResponse> getIssue(Long issueId) {
        final var issue = githubIssueReadRepository.findById(issueId)
                .orElseThrow(() -> notFound("Issue %s not found".formatted(issueId)));

        final var issueProjectIds = issue.repo().projects().stream().map(ProjectLinkReadEntity::id).toList();
        final var projectLedIds = authenticatedAppUserService.tryGetAuthenticatedUser()
                .map(user -> user.projectsLed().stream().toList())
                .orElse(List.of());

        final var asProjectLead = projectLedIds.stream().anyMatch(issueProjectIds::contains);
        return ok(issue.toDto(asProjectLead));
    }

    @Override
    public ResponseEntity<IssueApplicantsPageResponse> getIssueApplicants(Long issueId, ApplicantsQueryParams q) {
        final var applications = applicationReadRepository.findAllByIssueId(issueId,
                        q.getIsIgnored(),
                        q.getIsApplicantProjectMember(),
                        q.getSearch())
                .stream().collect(groupingBy(ApplicationReadEntity::applicantId));

        q.setContributorIds(applications.keySet().stream().toList());

        final var page = contributorKpisReadRepository.findAll(q);

        return ok(new IssueApplicantsPageResponse()
                .totalItemNumber((int) page.getTotalElements())
                .totalPageNumber(page.getTotalPages())
                .nextPageIndex(page.hasNext() ? page.getNumber() + 1 : page.getNumber())
                .hasMore(page.hasNext())
                .applicants(page.stream().map(c -> c.toIssueApplicant(applications.get(c.contributorId()).get(0))).toList())
        );
    }
}
