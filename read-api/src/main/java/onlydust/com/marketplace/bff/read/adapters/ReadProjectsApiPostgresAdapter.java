package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProjectsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectMoreInfoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoStatsViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.ContributionViewEntityRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomContributorRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomProjectRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.ProjectLeadViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ApplicationRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.bff.read.entities.github.GithubIssueReadEntity;
import onlydust.com.marketplace.bff.read.entities.project.ProjectReadEntity;
import onlydust.com.marketplace.bff.read.mapper.SponsorMapper;
import onlydust.com.marketplace.bff.read.mapper.UserMapper;
import onlydust.com.marketplace.bff.read.repositories.GithubIssueReadRepository;
import onlydust.com.marketplace.bff.read.repositories.ProjectReadRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.mapper.DateMapper;
import onlydust.com.marketplace.project.domain.model.ProjectRewardSettings;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.service.PermissionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectMapper.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadProjectsApiPostgresAdapter implements ReadProjectsApi {
    private static final int TOP_CONTRIBUTOR_COUNT = 3;

    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final PermissionService permissionService;
    private final GithubIssueReadRepository githubIssueReadRepository;
    private final ProjectReadRepository projectReadRepository;
    private final CustomProjectRepository customProjectRepository;
    private final CustomContributorRepository customContributorRepository;
    private final ProjectLeadViewRepository projectLeadViewRepository;
    private final ApplicationRepository applicationRepository;
    private final ContributionViewEntityRepository contributionViewEntityRepository;

    @Override
    public ResponseEntity<ProjectResponse> getProject(final UUID projectId, final Boolean includeAllAvailableRepos) {
        final var caller = authenticatedAppUserService.tryGetAuthenticatedUser().orElse(null);
        final var userId = caller == null ? null : caller.getId();
        if (!permissionService.hasUserAccessToProject(projectId, userId)) {
            throw OnlyDustException.forbidden("Project %s is private and user %s cannot access it".formatted(projectId, userId));
        }

        final var projectEntity = projectReadRepository.findById(projectId)
                .orElseThrow(() -> notFound(format("Project %s not found", projectId)));
        final var projectResponse = getProjectDetails(projectEntity, caller, includeAllAvailableRepos);

        return ok(projectResponse);
    }

    @Override
    public ResponseEntity<ProjectResponse> getProjectBySlug(final String slug, final Boolean includeAllAvailableRepos) {
        final var caller = authenticatedAppUserService.tryGetAuthenticatedUser().orElse(null);
        final var userId = caller == null ? null : caller.getId();
        if (!permissionService.hasUserAccessToProject(slug, userId)) {
            throw OnlyDustException.forbidden("Project %s is private and user %s cannot access it".formatted(slug, userId));
        }

        final var projectEntity = projectReadRepository.findBySlug(slug)
                .orElseThrow(() -> notFound(format("Project %s not found", slug)));
        final var projectResponse = getProjectDetails(projectEntity, caller, includeAllAvailableRepos);

        return ok(projectResponse);
    }

    @Override
    public ResponseEntity<GoodFirstIssuesPageResponse> getProjectGoodFirstIssues(UUID projectId, Integer pageIndex, Integer pageSize) {
        final var page = githubIssueReadRepository.findGoodFirstIssuesOf(projectId, PageRequest.of(pageIndex, pageSize, Sort.by("createdAt").descending()));
        return ok(new GoodFirstIssuesPageResponse()
                .issues(page.stream().map(GithubIssueReadEntity::toDto).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages())));
    }

    private ProjectResponse getProjectDetails(ProjectReadEntity project, User caller, final Boolean includeAllAvailableRepos) {
        final var topContributors = customContributorRepository.findProjectTopContributors(project.getId(), TOP_CONTRIBUTOR_COUNT);
        final var contributorCount = customContributorRepository.getProjectContributorCount(project.getId(), null);
        final var leaders = projectLeadViewRepository.findProjectLeadersAndInvitedLeaders(project.getId());
        final var ecosystems = customProjectRepository.getProjectEcosystems(project.getId());
        final var hasRemainingBudget = customProjectRepository.hasRemainingBudget(project.getId());
        final var reposIndexedTimes = project.getRepos().stream()
                .map(GithubRepoViewEntity::getStats)
                .filter(Objects::nonNull)
                .map(GithubRepoStatsViewEntity::getLastIndexedAt).toList();

        record Me(Boolean isLeader, Boolean isInvitedAsProjectLead, Boolean isContributor, Boolean hasApplied) {
            public Boolean isMember() {
                return isLeader || isInvitedAsProjectLead || isContributor;
            }
        }
        final var me = isNull(caller) ? null : new Me(
                leaders.stream().anyMatch(l -> l.getGithubId().equals(caller.getGithubUserId()) && l.getHasAcceptedInvitation()),
                leaders.stream().anyMatch(l -> l.getGithubId().equals(caller.getGithubUserId()) && !l.getHasAcceptedInvitation()),
                contributionViewEntityRepository.countBy(caller.getGithubUserId(), project.getId()) > 0,
                applicationRepository.findByProjectIdAndApplicantId(project.getId(), caller.getId()).isPresent()
        );

        return new ProjectResponse()
                .id(project.getId())
                .slug(project.getSlug())
                .name(project.getName())
                .createdAt(DateMapper.ofNullable(project.getCreatedAt()))
                .shortDescription(project.getShortDescription())
                .longDescription(project.getLongDescription())
                .logoUrl(project.getLogoUrl())
                .moreInfos(isNull(project.getMoreInfos()) ? null : project.getMoreInfos().stream()
                        .sorted(Comparator.comparing(ProjectMoreInfoViewEntity::getRank))
                        .map(moreInfo -> new SimpleLink().url(moreInfo.getUrl()).value(moreInfo.getName()))
                        .collect(Collectors.toList()))
                .hiring(project.getHiring())
                .visibility(mapProjectVisibility(project.getVisibility()))
                .contributorCount(contributorCount)
                .hasRemainingBudget(hasRemainingBudget)
                .rewardSettings(mapRewardSettings(new ProjectRewardSettings(
                        project.getIgnorePullRequests(),
                        project.getIgnoreIssues(),
                        project.getIgnoreCodeReviews(),
                        project.getIgnoreContributionsBefore()
                )))
                .topContributors(topContributors.stream().map(u -> new GithubUserResponse()
                        .githubUserId(u.getGithubUserId())
                        .login(u.getLogin())
                        .avatarUrl(u.getAvatarUrl())
                ).toList())
                .leaders(leaders.stream()
                        .filter(leader -> Boolean.TRUE.equals(leader.getHasAcceptedInvitation()))
                        .map(UserMapper::map)
                        .sorted(comparing(RegisteredUserResponse::getGithubUserId))
                        .collect(Collectors.toList()))
                .invitedLeaders(leaders.stream()
                        .filter(leader -> Boolean.FALSE.equals(leader.getHasAcceptedInvitation()))
                        .map(UserMapper::map)
                        .sorted(comparing(RegisteredUserResponse::getGithubUserId))
                        .collect(Collectors.toList()))
                .tags(project.getTags().stream()
                        .map(t -> switch (t.getTag()) {
                            case HOT_COMMUNITY -> ProjectTag.HOT_COMMUNITY;
                            case NEWBIES_WELCOME -> ProjectTag.NEWBIES_WELCOME;
                            case LIKELY_TO_REWARD -> ProjectTag.LIKELY_TO_REWARD;
                            case WORK_IN_PROGRESS -> ProjectTag.WORK_IN_PROGRESS;
                            case FAST_AND_FURIOUS -> ProjectTag.FAST_AND_FURIOUS;
                            case BIG_WHALE -> ProjectTag.BIG_WHALE;
                            case UPDATED_ROADMAP -> ProjectTag.UPDATED_ROADMAP;
                        })
                        .sorted(comparing(ProjectTag::name))
                        .toList())
                .ecosystems(ecosystems.stream()
                        .map(e -> new EcosystemResponse()
                                .id(e.getId())
                                .name(e.getName())
                                .slug(e.getSlug())
                                .logoUrl(e.getLogoUrl())
                                .url(e.getUrl())
                        )
                        .sorted(comparing(EcosystemResponse::getName))
                        .toList())
                .categories(project.getCategories().stream()
                        .map(c -> new ProjectCategoryResponse()
                                .id(c.getId())
                                .name(c.getName())
                                .iconSlug(c.getIconSlug())
                        )
                        .sorted(comparing(ProjectCategoryResponse::getName))
                        .toList())
                .sponsors(project.getSponsors().stream()
                        .filter(s -> SponsorMapper.isActive(s))
                        .map(s -> s.sponsor())
                        .map(s -> new SponsorResponse()
                                .id(s.getId())
                                .name(s.getName())
                                .logoUrl(s.getLogoUrl())
                                .url(s.getUrl())
                        )
                        .sorted(comparing(SponsorResponse::getName))
                        .toList())
                .organizations(project.organizations().stream()
                        .map(organizationView -> mapOrganization(organizationView, Boolean.TRUE.equals(includeAllAvailableRepos)))
                        .sorted(comparing(GithubOrganizationResponse::getGithubUserId))
                        .toList())
                .technologies(project.technologies())
                .indexingComplete(reposIndexedTimes.stream().noneMatch(Objects::isNull))
                .indexedAt(reposIndexedTimes.stream().filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null))
                .me(me == null ? null : new ProjectMeResponse()
                        .isMember(me.isMember())
                        .isProjectLead(me.isLeader())
                        .isInvitedAsProjectLead(me.isInvitedAsProjectLead())
                        .isContributor(me.isContributor())
                        .hasApplied(me.hasApplied()));
    }
}