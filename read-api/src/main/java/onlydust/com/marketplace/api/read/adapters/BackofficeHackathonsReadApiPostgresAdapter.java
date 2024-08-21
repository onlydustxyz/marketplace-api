package onlydust.com.marketplace.api.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeHackathonsReadApi;
import onlydust.com.backoffice.api.contract.model.HackathonsDetailsResponse;
import onlydust.com.backoffice.api.contract.model.HackathonsPageResponse;
import onlydust.com.backoffice.api.contract.model.UserPage;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonItemReadEntity;
import onlydust.com.marketplace.api.read.entities.hackathon.HackathonReadEntity;
import onlydust.com.marketplace.api.read.mapper.UserMapper;
import onlydust.com.marketplace.api.read.repositories.HackathonItemReadRepository;
import onlydust.com.marketplace.api.read.repositories.HackathonReadRepository;
import onlydust.com.marketplace.api.read.repositories.UserReadRepository;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("bo")
public class BackofficeHackathonsReadApiPostgresAdapter implements BackofficeHackathonsReadApi {

    private UserReadRepository userReadRepository;
    private HackathonReadRepository hackathonReadRepository;
    private HackathonItemReadRepository hackathonItemReadRepository;

    @Override
    public ResponseEntity<UserPage> getRegisteredUserPage(UUID hackathonId, Integer pageIndex, Integer pageSize, String login) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);
        final var usersPage = userReadRepository.findAllRegisteredOnHackathon(login == null ? "" : login, hackathonId,
                PageRequest.of(sanitizedPageIndex, sanitizePageSize, JpaSort.unsafe("hr.techCreatedAt").descending()));
        final var response = UserMapper.pageToResponse(usersPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<HackathonsDetailsResponse> getHackathonById(UUID hackathonId) {
        return ResponseEntity.ok(
                hackathonReadRepository.findById(hackathonId)
                        .orElseThrow(() -> OnlyDustException.notFound("Hackathon %s not found".formatted(hackathonId)))
                        .toBoResponse()
        );
    }

    @Override
    public ResponseEntity<HackathonsPageResponse> getHackathons(Integer pageIndex, Integer pageSize) {
        final int sanitizePageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);
        final var page = hackathonItemReadRepository.findAll(PageRequest.of(sanitizePageIndex, sanitizePageSize,
                Sort.by(Sort.Direction.DESC, "start_date")));

        final HackathonsPageResponse hackathonsPageResponse = new HackathonsPageResponse();
        hackathonsPageResponse.setNextPageIndex(PaginationHelper.nextPageIndex(sanitizePageIndex, page.getTotalPages()));
        hackathonsPageResponse.setHasMore(PaginationHelper.hasMore(sanitizePageIndex, page.getTotalPages()));
        hackathonsPageResponse.setTotalPageNumber(page.getTotalPages());
        hackathonsPageResponse.setTotalItemNumber((int) page.getTotalElements());
        page.stream()
                .map(HackathonItemReadEntity::toHackathonsPageItemResponse)
                .forEach(hackathonsPageResponse::addHackathonsItem);

        return hackathonsPageResponse.getHasMore() ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(hackathonsPageResponse) :
                ResponseEntity.ok(hackathonsPageResponse);
    }
}
