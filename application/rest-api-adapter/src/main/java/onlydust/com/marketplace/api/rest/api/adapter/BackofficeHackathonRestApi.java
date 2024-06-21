package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeHackathonManagementApi;
import onlydust.com.backoffice.api.contract.model.CreateHackathonRequest;
import onlydust.com.backoffice.api.contract.model.PatchHackathonRequest;
import onlydust.com.backoffice.api.contract.model.UpdateHackathonRequest;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.HackathonMapper;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.port.input.HackathonFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tags(@Tag(name = "BackofficeHackathonManagement"))
@AllArgsConstructor
@Profile("bo")
public class BackofficeHackathonRestApi implements BackofficeHackathonManagementApi {

    private final HackathonFacadePort hackathonFacadePort;

    @Override
    public ResponseEntity<Void> createHackathon(CreateHackathonRequest request) {
        hackathonFacadePort.createHackathon(request.getTitle(), request.getSubtitle(), request.getStartDate(),
                request.getEndDate());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateHackathonById(UUID hackathonId, UpdateHackathonRequest updateHackathonRequest) {
        hackathonFacadePort.updateHackathon(HackathonMapper.toDomain(hackathonId, updateHackathonRequest));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> patchHackathonById(UUID hackathonId, PatchHackathonRequest patchHackathonRequest) {
        hackathonFacadePort.updateHackathonStatus(hackathonId, Hackathon.Status.valueOf(patchHackathonRequest.getStatus().name()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteHackathonById(UUID hackathonId) {
        hackathonFacadePort.deleteHackathon(Hackathon.Id.of(hackathonId));
        return ResponseEntity.noContent().build();
    }
}
