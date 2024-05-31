package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadProjectsApi;
import onlydust.com.marketplace.api.contract.model.GoodFirstIssuesPageResponse;
import onlydust.com.marketplace.bff.read.entities.github.GithubIssueReadEntity;
import onlydust.com.marketplace.bff.read.repositories.GithubIssueReadRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadProjectsApiPostgresAdapter implements ReadProjectsApi {
    private final GithubIssueReadRepository githubIssueReadRepository;

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
}
