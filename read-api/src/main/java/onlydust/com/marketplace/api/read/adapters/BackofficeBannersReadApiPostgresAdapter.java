package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeBannersReadApi;
import onlydust.com.backoffice.api.contract.model.BannerPageResponse;
import onlydust.com.marketplace.api.read.entities.BannerReadEntity;
import onlydust.com.marketplace.api.read.repositories.BannerReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("bo")
public class BackofficeBannersReadApiPostgresAdapter implements BackofficeBannersReadApi {
    private final BannerReadRepository bannerReadRepository;

    @Override
    public ResponseEntity<BannerPageResponse> getBannerPage(Integer pageIndex, Integer pageSize) {
        final var page = bannerReadRepository.findAll(PageRequest.of(pageIndex, pageSize, Sort.by("updatedAt").descending()));

        final var response = new BannerPageResponse()
                .banners(page.stream().map(BannerReadEntity::toBoPageItemResponse).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));

        return response.getHasMore() ? status(PARTIAL_CONTENT).body(response) : ok(response);
    }
}
