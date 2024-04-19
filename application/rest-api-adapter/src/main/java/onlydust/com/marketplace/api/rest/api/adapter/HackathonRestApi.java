package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.HackathonsApi;
import onlydust.com.marketplace.api.contract.model.HackathonsDetailsResponse;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.HackathonMapper;
import onlydust.com.marketplace.project.domain.port.input.HackathonFacadePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Hackathons"))
@AllArgsConstructor
public class HackathonRestApi implements HackathonsApi {

    private final HackathonFacadePort hackathonFacadePort;

    @Override
    public ResponseEntity<HackathonsDetailsResponse> getHackathonBySlug(String hackathonSlug) {
        final var hackathonDetailsView = hackathonFacadePort.getHackathonBySlug(hackathonSlug);
        return ResponseEntity.ok(HackathonMapper.toResponse(hackathonDetailsView));
    }
}
