package onlydust.com.marketplace.api.read.adapters;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.read.cache.Cache.S;
import static onlydust.com.marketplace.api.read.cache.Cache.XS;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadHackathonsApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.cache.Cache;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonItemReadEntity;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonProjectIssuesReadEntity;
import onlydust.com.marketplace.api.read.entities.project.ProjectPageV2ItemQueryEntity;
import onlydust.com.marketplace.api.read.repositories.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

@RestController
@Tags(@Tag(name = "Hackathons"))
@AllArgsConstructor
@Profile("api")
@Transactional(readOnly = true)
public class ReadHackathonsApiPostgresAdapter implements ReadHackathonsApi {

    private final Cache cache;
    private final HackathonReadRepository hackathonReadRepository;
    private final HackathonProjectIssuesReadRepository hackathonProjectIssuesReadRepository;
    private final LanguageReadRepository languageReadRepository;
    private final HackathonItemReadRepository hackathonItemReadRepository;
    private final HackathonV2ReadRepository hackathonV2ReadRepository;
    private final ProjectsPageV2Repository projectsPageV2Repository;

    @Override
    public ResponseEntity<HackathonsDetailsResponse> getHackathonBySlug(String hackathonSlug) {
        final var hackathon = hackathonReadRepository.findBySlug(hackathonSlug)
                .orElseThrow(() -> OnlyDustException.notFound("Hackathon not found for slug %s".formatted(hackathonSlug)));
        return ok()
                .cacheControl(cache.forEverybody(S))
                .body(hackathon.toResponse());
    }

    @Override
    public ResponseEntity<HackathonResponseV2> getHackathonBySlugV2(String hackathonSlug) {
        final var hackathon = hackathonV2ReadRepository.findBySlug(hackathonSlug)
                .orElseThrow(() -> OnlyDustException.notFound("Hackathon not found for slug %s".formatted(hackathonSlug)));
        return ok()
                .body(hackathon.toResponse());
    }

    @Override
    public ResponseEntity<ProjectPageResponseV2> getHackathonProjects(
        String hackathonSlug,
        Integer pageIndex,
        Integer pageSize,
        String search,
        List<UUID> languageIds,
        List<UUID> ecosystemIds,
        Boolean hasAvailableIssues
    ) {       
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var sanitizedPageSize = sanitizePageSize(pageSize);
        final var page = projectsPageV2Repository.findHackathonProjects(hackathonSlug, 
            isNull(languageIds) ? null : languageIds.stream().distinct().toArray(UUID[]::new),
            isNull(ecosystemIds) ? null : ecosystemIds.stream().distinct().toArray(UUID[]::new),
            hasAvailableIssues,
            search,
            PageRequest.of(sanitizedPageIndex, sanitizedPageSize));

        return ok()
                .body(new ProjectPageResponseV2()
                        .projects(page.getContent().stream().map(ProjectPageV2ItemQueryEntity::toShortResponse).toList())
                        .totalPageNumber(page.getTotalPages())
                        .totalItemNumber((int) page.getTotalElements())
                        .hasMore(page.hasNext()));
    }

    @Override
    public ResponseEntity<HackathonsListResponse> getHackathons() {
        final var hackathonsListResponse = new HackathonsListResponse();
        hackathonItemReadRepository.findAllPublished(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "start_date")))
                .stream()
                .map(HackathonItemReadEntity::toHackathonsListItemResponse)
                .forEach(hackathonsListResponse::addHackathonsItem);
        return ok()
                .cacheControl(cache.forEverybody(S))
                .body(hackathonsListResponse);
    }

    @Override
    public ResponseEntity<HackathonProjectsIssuesResponse> getHackathonIssues(UUID hackathonId,
                                                                              List<UUID> languageIds,
                                                                              List<GithubIssueStatus> statuses,
                                                                              Boolean isAssigned,
                                                                              Boolean isApplied,
                                                                              Boolean isAvailable,
                                                                              Boolean isGoodFirstIssue,
                                                                              String search) {
        final var hackathonProjectIssues = hackathonProjectIssuesReadRepository.findAll(hackathonId,
                isNull(statuses) ? null : statuses.stream().distinct().map(s -> switch (s) {
                    case OPEN -> ContributionStatus.IN_PROGRESS;
                    case COMPLETED -> ContributionStatus.COMPLETED;
                    case CANCELLED -> ContributionStatus.CANCELLED;
                }).map(ContributionStatus::name).toArray(String[]::new),
                isAssigned,
                isApplied,
                isAvailable,
                isGoodFirstIssue,
                isNull(languageIds) ? null : languageIds.stream().distinct().toArray(UUID[]::new),
                search);
        final var languageEntities = languageReadRepository.findAllByHackathonId(hackathonId);
        return ok()
                .cacheControl(cache.forEverybody(XS))
                .body(new HackathonProjectsIssuesResponse()
                        .languages(languageEntities.stream()
                                .map(LanguageReadEntity::toDto)
                                .toList())
                        .projects(hackathonProjectIssues.stream()
                                .map(HackathonProjectIssuesReadEntity::toDto)
                                .toList()));
    }
}
