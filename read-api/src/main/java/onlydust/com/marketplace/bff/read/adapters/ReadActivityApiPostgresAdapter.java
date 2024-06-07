package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadActivityApi;
import onlydust.com.marketplace.api.contract.model.PublicActivityPageResponse;
import onlydust.com.marketplace.bff.read.repositories.ActivityReadRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReadActivityApiPostgresAdapter implements ReadActivityApi {

    private final ActivityReadRepository activityReadRepository;

    @Override
    public ResponseEntity<PublicActivityPageResponse> getPublicActivity(Integer pageIndex, Integer pageSize) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);
        final var page = activityReadRepository.findLastActivity(PageRequest.of(sanitizedPageIndex, sanitizePageSize));
        return ResponseEntity.ok(new PublicActivityPageResponse()
                .activities(page.getContent().stream().map(activity -> activity.toDto()).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages())));
    }
}
