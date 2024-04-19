package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.HackathonsApi;
import onlydust.com.marketplace.api.contract.model.HackathonsDetailsResponse;
import onlydust.com.marketplace.api.contract.model.HackathonsListResponse;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.HackathonMapper;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.input.HackathonFacadePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Hackathons"))
@AllArgsConstructor
public class HackathonRestApi implements HackathonsApi {

    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final HackathonFacadePort hackathonFacadePort;

    @Override
    public ResponseEntity<HackathonsDetailsResponse> getHackathonBySlug(String hackathonSlug) {
        final var authenticatedUser = authenticatedAppUserService.tryGetAuthenticatedUser();
        final var hackathonDetailsView = hackathonFacadePort.getHackathonBySlug(hackathonSlug);
        final var isRegistered = authenticatedUser.map(User::getId)
                .map(userId -> hackathonFacadePort.isRegisteredToHackathon(userId, hackathonDetailsView.id()));
        return ResponseEntity.ok(HackathonMapper.toResponse(hackathonDetailsView, isRegistered));
    }

    @Override
    public ResponseEntity<HackathonsListResponse> getHackathons() {
        final var hackathons = hackathonFacadePort.getAllPublishedHackathons();
        return ResponseEntity.ok(HackathonMapper.map(hackathons));
    }
}
