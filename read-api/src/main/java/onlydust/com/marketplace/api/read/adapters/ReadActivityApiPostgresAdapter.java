package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadActivityApi;
import onlydust.com.marketplace.api.contract.model.PublicActivityPageResponse;
import onlydust.com.marketplace.api.read.entities.RecentPublicActivityReadEntity;
import onlydust.com.marketplace.api.read.properties.Cache;
import onlydust.com.marketplace.api.read.repositories.RecentPublicActivityReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.api.read.properties.Cache.S;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("api")
public class ReadActivityApiPostgresAdapter implements ReadActivityApi {
    private final Cache cache;
    private final RecentPublicActivityReadRepository recentPublicActivityReadRepository;

    @Override
    public ResponseEntity<PublicActivityPageResponse> getPublicActivity(Integer pageIndex, Integer pageSize) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);
        final var page = recentPublicActivityReadRepository.findLastActivity(PageRequest.of(sanitizedPageIndex, sanitizePageSize));
        return ResponseEntity.ok()
                .cacheControl(cache.forEverybody(S))
                .body(new PublicActivityPageResponse()
                        .activities(page.getContent().stream().map(RecentPublicActivityReadEntity::toDto).toList())
                        .totalPageNumber(page.getTotalPages())
                        .totalItemNumber((int) page.getTotalElements())
                        .hasMore(hasMore(pageIndex, page.getTotalPages()))
                        .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages())));
    }
}
