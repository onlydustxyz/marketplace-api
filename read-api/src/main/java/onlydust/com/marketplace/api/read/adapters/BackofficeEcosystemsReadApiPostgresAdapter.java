package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeEcosystemReadApi;
import onlydust.com.backoffice.api.contract.model.EcosystemPage;
import onlydust.com.backoffice.api.contract.model.EcosystemResponse;
import onlydust.com.marketplace.api.read.entities.ecosystem.EcosystemReadEntity;
import onlydust.com.marketplace.api.read.repositories.EcosystemReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("bo")
public class BackofficeEcosystemsReadApiPostgresAdapter implements BackofficeEcosystemReadApi {
    private final EcosystemReadRepository ecosystemReadRepository;

    @Override
    public ResponseEntity<EcosystemResponse> getEcosystem(UUID ecosystemId) {
        final var ecosystem = ecosystemReadRepository.findById(ecosystemId)
                .orElseThrow(() -> notFound("Ecosystem %s not found".formatted(ecosystemId)));

        return ok(ecosystem.toBoDetailsResponse());
    }

    @Override
    public ResponseEntity<EcosystemPage> getEcosystems(Integer pageIndex, Integer pageSize, String search) {
        final var index = sanitizePageIndex(pageIndex);
        final var size = sanitizePageSize(pageSize);

        final var page = ecosystemReadRepository.findAllByName(search, PageRequest.of(index, size, Sort.by("name")));

        final var response = new EcosystemPage()
                .ecosystems(page.getContent().stream().map(EcosystemReadEntity::toBoPageItemResponse).toList())
                .hasMore(hasMore(index, page.getTotalPages()))
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .nextPageIndex(nextPageIndex(index, page.getTotalPages()));

        return status(response.getHasMore() ? PARTIAL_CONTENT : OK).body(response);
    }
}
