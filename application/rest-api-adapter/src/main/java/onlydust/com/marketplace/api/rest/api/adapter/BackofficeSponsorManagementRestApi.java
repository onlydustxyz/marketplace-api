package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeSponsorManagementApi;
import onlydust.com.backoffice.api.contract.model.SponsorPage;
import onlydust.com.backoffice.api.contract.model.SponsorResponse;
import onlydust.com.marketplace.api.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.api.domain.view.backoffice.SponsorView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapSponsorPageToContract;

@RestController
@Tags(@Tag(name = "BackofficeSponsorManagement"))
@AllArgsConstructor
public class BackofficeSponsorManagementRestApi implements BackofficeSponsorManagementApi {

    private final BackofficeFacadePort backofficeFacadePort;
    final static Integer MAX_PAGE_SIZE = Integer.MAX_VALUE;

    @Override
    public ResponseEntity<SponsorPage> getSponsorPage(Integer pageIndex, Integer pageSize,
                                                      List<UUID> projectIds, List<UUID> sponsorIds) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);

        final var filters = SponsorView.Filters.builder()
                .projects(Optional.ofNullable(projectIds).orElse(List.of()))
                .sponsors(Optional.ofNullable(sponsorIds).orElse(List.of()))
                .build();

        final var sponsorPage =
                backofficeFacadePort.listSponsors(sanitizedPageIndex, sanitizePageSize(pageSize, MAX_PAGE_SIZE), filters);

        final var response = mapSponsorPageToContract(sponsorPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SponsorResponse> getSponsor(UUID sponsorId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
