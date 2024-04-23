package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeHackathonManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.HackathonMapper;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.input.HackathonFacadePort;
import onlydust.com.marketplace.project.domain.view.HackathonShortView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumSet;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "BackofficeHackathonManagement"))
@AllArgsConstructor
public class BackofficeHackathonRestApi implements BackofficeHackathonManagementApi {

    private final HackathonFacadePort hackathonFacadePort;

    @Override
    public ResponseEntity<HackathonsDetailsResponse> createHackathon(CreateHackathonRequest request) {
        final var hackathonDetailsView = hackathonFacadePort.createHackathon(request.getTitle(), request.getSubtitle(), request.getStartDate(),
                request.getEndDate());
        return ResponseEntity.ok(HackathonMapper.toBackOfficeResponse(hackathonDetailsView));
    }

    @Override
    public ResponseEntity<HackathonsDetailsResponse> updateHackathonById(UUID hackathonId, UpdateHackathonRequest updateHackathonRequest) {
        hackathonFacadePort.updateHackathon(HackathonMapper.toDomain(hackathonId, updateHackathonRequest));
        final var hackathonDetailsView = hackathonFacadePort.getHackathonById(Hackathon.Id.of(hackathonId));
        return ResponseEntity.ok(HackathonMapper.toBackOfficeResponse(hackathonDetailsView));
    }

    @Override
    public ResponseEntity<HackathonsDetailsResponse> patchHackathonById(UUID hackathonId, PatchHackathonRequest patchHackathonRequest) {
        hackathonFacadePort.updateHackathonStatus(hackathonId, Hackathon.Status.valueOf(patchHackathonRequest.getStatus().name()));
        final var hackathonDetailsView = hackathonFacadePort.getHackathonById(Hackathon.Id.of(hackathonId));
        return ResponseEntity.ok(HackathonMapper.toBackOfficeResponse(hackathonDetailsView));
    }

    @Override
    public ResponseEntity<HackathonsDetailsResponse> getHackathonById(UUID hackathonId) {
        final var hackathonDetailsView = hackathonFacadePort.getHackathonById(Hackathon.Id.of(hackathonId));
        return ResponseEntity.ok(HackathonMapper.toBackOfficeResponse(hackathonDetailsView));
    }

    @Override
    public ResponseEntity<HackathonsPageResponse> getHackathons(Integer pageIndex, Integer pageSize) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final Page<HackathonShortView> hackathons = hackathonFacadePort.getHackathons(sanitizedPageIndex, sanitizedPageSize,
                EnumSet.allOf(Hackathon.Status.class));
        return ResponseEntity.ok(HackathonMapper.map(sanitizedPageIndex, hackathons));
    }

    @Override
    public ResponseEntity<Void> deleteHackathonById(UUID hackathonId) {
        hackathonFacadePort.deleteHackathon(Hackathon.Id.of(hackathonId));
        return ResponseEntity.noContent().build();
    }
}
