package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeHackathonManagementApi;
import onlydust.com.backoffice.api.contract.model.CreateHackathonRequest;
import onlydust.com.marketplace.project.domain.port.input.HackathonFacadePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "BackofficeHackathonManagement"))
@AllArgsConstructor
public class BackofficeHackathonRestApi implements BackofficeHackathonManagementApi {

    private final HackathonFacadePort hackathonFacadePort;

    @Override
    public ResponseEntity<Void> createHackathon(CreateHackathonRequest request) {
        hackathonFacadePort.createHackathon(request.getTitle(), request.getSubtitle(), request.getStartDate(), request.getEndDate());
        return ResponseEntity.noContent().build();
    }
}
