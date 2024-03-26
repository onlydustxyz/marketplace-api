package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeSponsorManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.SponsorMapper;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.project.domain.view.backoffice.SponsorView;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "BackofficeSponsorManagement"))
@AllArgsConstructor
public class BackofficeSponsorManagementRestApi implements BackofficeSponsorManagementApi {

    final static Integer MAX_PAGE_SIZE = Integer.MAX_VALUE;
    private final BackofficeFacadePort backofficeFacadePort;
    private final SponsorFacadePort sponsorFacadePort;
    private final AccountingFacadePort accountingFacadePort;

    @Override
    public ResponseEntity<SponsorResponse> createSponsor(SponsorRequest sponsorRequest) {
        final var sponsor = backofficeFacadePort.createSponsor(sponsorRequest.getName(),
                sponsorRequest.getUrl(),
                sponsorRequest.getLogoUrl());
        return ResponseEntity.ok(mapSponsorToResponse(sponsor));
    }

    @Override
    public ResponseEntity<SponsorResponse> updateSponsor(UUID sponsorId, SponsorRequest sponsorRequest) {
        final var sponsor = backofficeFacadePort.updateSponsor(sponsorId,
                sponsorRequest.getName(),
                sponsorRequest.getUrl(),
                sponsorRequest.getLogoUrl());
        return ResponseEntity.ok(mapSponsorToResponse(sponsor));
    }

    @Override
    public ResponseEntity<SponsorPage> getSponsorPage(Integer pageIndex, Integer pageSize) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final var sponsorPage =
                sponsorFacadePort.listSponsors(sanitizedPageIndex, sanitizedPageSize);

        final var response = SponsorMapper.sponsorPageToResponse(sponsorPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SponsorDetailsResponse> getSponsor(UUID sponsorId) {
        final var sponsor = backofficeFacadePort.getSponsor(sponsorId)
                .orElseThrow(() -> OnlyDustException.notFound("Sponsor %s not found".formatted(sponsorId)));
        final var sponsorAccountStatements = accountingFacadePort.getSponsorAccounts(SponsorId.of(sponsorId));
        return ResponseEntity.ok(mapSponsorToDetailsResponse(sponsor, sponsorAccountStatements));
    }

    @Override
    public ResponseEntity<OldSponsorPage> getOldSponsorPage(Integer pageIndex, Integer pageSize, List<UUID> projectIds, List<UUID> sponsorIds) {
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
    public ResponseEntity<UploadImageResponse> uploadSponsorLogo(Resource image) {
        InputStream imageInputStream;
        try {
            imageInputStream = image.getInputStream();
        } catch (IOException e) {
            throw badRequest("Error while reading image data", e);
        }

        final URL imageUrl = sponsorFacadePort.uploadLogo(imageInputStream);
        final var response = new UploadImageResponse().url(imageUrl.toString());

        return ResponseEntity.ok(response);
    }
}
