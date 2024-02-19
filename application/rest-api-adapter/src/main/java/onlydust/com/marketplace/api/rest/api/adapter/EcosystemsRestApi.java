package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.EcosystemsApi;
import onlydust.com.marketplace.api.contract.model.EcosystemPage;
import onlydust.com.marketplace.api.domain.model.Ecosystem;
import onlydust.com.marketplace.api.domain.port.input.EcosystemFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.EcosystemMapper;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Ecosystems"))
@AllArgsConstructor
public class EcosystemsRestApi implements EcosystemsApi {
    private final EcosystemFacadePort ecosystemFacadePort;

    @Override
    public ResponseEntity<EcosystemPage> getAllEcosystems(Integer pageIndex, Integer pageSize) {
        final int sanitizePageIndex = PaginationHelper.sanitizePageIndex(pageIndex);
        final int sanitizedPageSize = PaginationHelper.sanitizePageSize(pageSize);
        final Page<Ecosystem> ecosystemPage = ecosystemFacadePort.findAll(sanitizePageIndex, sanitizedPageSize);
        final EcosystemPage response = EcosystemMapper.mapToResponse(ecosystemPage, pageIndex);
        return ecosystemPage.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }
}
