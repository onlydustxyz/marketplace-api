package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeHackathonsReadApi;
import onlydust.com.backoffice.api.contract.model.UserPage;
import onlydust.com.marketplace.bff.read.mapper.UserMapper;
import onlydust.com.marketplace.bff.read.repositories.UserShortRepository;
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
public class BackofficeHackathonsReadApiPostgresAdapter implements BackofficeHackathonsReadApi {

    private UserShortRepository userShortRepository;

    @Override
    public ResponseEntity<UserPage> getRegisteredUserPage(UUID hackathonId, Integer pageIndex, Integer pageSize, String login) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);
        final var usersPage = userShortRepository.findAllRegisteredOnHackathon(login, hackathonId,
                PageRequest.of(sanitizedPageIndex, sanitizePageSize, JpaSort.unsafe(Sort.Direction.DESC, "tech_created_at")));
        final var response = UserMapper.pageToResponse(usersPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }
}
