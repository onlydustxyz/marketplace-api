package onlydust.com.marketplace.api.read.adapters;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadHackathonsApi;
import onlydust.com.marketplace.api.contract.model.HackathonsDetailsResponse;
import onlydust.com.marketplace.api.contract.model.HackathonsListResponse;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonReadEntity;
import onlydust.com.marketplace.api.read.repositories.HackathonReadRepository;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.model.User;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Hackathons"))
@AllArgsConstructor
@Profile("api")
@Transactional(readOnly = true)
public class ReadHackathonsApiPostgresAdapter implements ReadHackathonsApi {

    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final HackathonReadRepository hackathonReadRepository;

    @Override
    public ResponseEntity<HackathonsDetailsResponse> getHackathonBySlug(String hackathonSlug) {
        final var authenticatedUser = authenticatedAppUserService.tryGetAuthenticatedUser();
        final var hackathon = hackathonReadRepository.findBySlug(hackathonSlug)
                .orElseThrow(() -> OnlyDustException.notFound("Hackathon not found for slug %s".formatted(hackathonSlug)));
        final Boolean isRegistered = authenticatedUser.map(User::getId)
                .map(userId -> hackathonReadRepository.isRegisteredToHackathon(userId, hackathon.id()))
                .orElse(null);
        return ResponseEntity.ok(hackathon.toResponse(isRegistered));
    }

    @Override
    public ResponseEntity<HackathonsListResponse> getHackathons() {
        final HackathonsListResponse hackathonsListResponse = new HackathonsListResponse();
        hackathonReadRepository.findAllPublished(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "startDate")))
                .stream()
                .map(HackathonReadEntity::toHackathonsListItemResponse)
                .forEach(hackathonsListResponse::addHackathonsItem);
        return ResponseEntity.ok(hackathonsListResponse);
    }
}
