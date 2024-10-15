package onlydust.com.marketplace.api.read.adapters;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadHackathonsApi;
import onlydust.com.marketplace.api.contract.model.GithubIssueStatus;
import onlydust.com.marketplace.api.contract.model.HackathonProjectsIssuesResponse;
import onlydust.com.marketplace.api.contract.model.HackathonsDetailsResponse;
import onlydust.com.marketplace.api.contract.model.HackathonsListResponse;
import onlydust.com.marketplace.api.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonItemReadEntity;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonProjectIssuesReadEntity;
import onlydust.com.marketplace.api.read.properties.Cache;
import onlydust.com.marketplace.api.read.repositories.HackathonItemReadRepository;
import onlydust.com.marketplace.api.read.repositories.HackathonProjectIssuesReadRepository;
import onlydust.com.marketplace.api.read.repositories.HackathonReadRepository;
import onlydust.com.marketplace.api.read.repositories.LanguageReadRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.read.properties.Cache.S;
import static onlydust.com.marketplace.api.read.properties.Cache.XS;
import static org.springframework.http.ResponseEntity.ok;

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

    @Override
    public ResponseEntity<HackathonsDetailsResponse> getHackathonBySlug(String hackathonSlug) {
        final var hackathon = hackathonReadRepository.findBySlug(hackathonSlug)
                .orElseThrow(() -> OnlyDustException.notFound("Hackathon not found for slug %s".formatted(hackathonSlug)));
        return ok()
                .cacheControl(cache.forEverybody(S))
                .body(hackathon.toResponse());
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
                isNull(statuses) ? null : statuses.stream().distinct().map(GithubIssueStatus::name).toArray(String[]::new),
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
