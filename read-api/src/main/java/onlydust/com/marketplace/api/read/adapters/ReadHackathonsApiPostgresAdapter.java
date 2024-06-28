package onlydust.com.marketplace.api.read.adapters;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.ReadHackathonsApi;
import onlydust.com.marketplace.api.contract.model.HackathonsDetailsResponse;
import onlydust.com.marketplace.api.contract.model.HackathonsListResponse;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonDetailsReadEntity;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonShortReadEntity;
import onlydust.com.marketplace.api.read.repositories.HackathonDetailsReadRepository;
import onlydust.com.marketplace.api.read.repositories.HackathonShortReadRepository;
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
    private final HackathonShortReadRepository hackathonShortReadRepository;
    private final HackathonDetailsReadRepository hackathonDetailsReadRepository;

    @Override
    public ResponseEntity<HackathonsDetailsResponse> getHackathonBySlug(String hackathonSlug) {
        final var authenticatedUser = authenticatedAppUserService.tryGetAuthenticatedUser();
        final HackathonDetailsReadEntity hackathonDetailsReadEntity =
                hackathonDetailsReadRepository.findBySlug(hackathonSlug).orElseThrow(() -> OnlyDustException.notFound("Hackathon not found for slug %s".formatted(hackathonSlug)));
        final Boolean isRegistered = authenticatedUser.map(User::getId)
                .map(userId -> hackathonShortReadRepository.isRegisteredToHackathon(userId, hackathonDetailsReadEntity.getId()))
                .orElse(null);
        return ResponseEntity.ok(hackathonDetailsReadEntity.toResponse(isRegistered));
    }

    @Override
    public ResponseEntity<HackathonsListResponse> getHackathons() {
        final HackathonsListResponse hackathonsListResponse = new HackathonsListResponse();
        hackathonShortReadRepository.findAllPublished(PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC,
                        "start_date")))
                .stream()
                .map(HackathonShortReadEntity::toHackathonsListItemResponse)
                .forEach(hackathonsListResponse::addHackathonsItem);
        return ResponseEntity.ok(hackathonsListResponse);
    }
}
