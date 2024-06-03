package onlydust.com.marketplace.bff.read.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeUsersReadApi;
import onlydust.com.backoffice.api.contract.model.UserDetailsResponse;
import onlydust.com.backoffice.api.contract.model.UserPage;
import onlydust.com.marketplace.bff.read.mapper.UserMapper;
import onlydust.com.marketplace.bff.read.repositories.UserReadRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class BackofficeUsersReadApiPostgresAdapter implements BackofficeUsersReadApi {

    private UserReadRepository userReadRepository;

    @Override
    public ResponseEntity<UserDetailsResponse> getUserById(UUID userId) {
        final var user = userReadRepository.findByUserId(userId)
                .orElseThrow(() -> notFound("User %s not found".formatted(userId)));

        return ResponseEntity.ok(user.toUserDetailsResponse());
    }

    @Override
    public ResponseEntity<UserPage> getUserPage(Integer pageIndex, Integer pageSize, String login) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final int sanitizePageSize = sanitizePageSize(pageSize);
        final var usersPage = userReadRepository.findAllRegisteredByLogin(login == null ? "" : login,
                PageRequest.of(sanitizedPageIndex, sanitizePageSize, Sort.by("registered.createdAt").descending()));
        final var response = UserMapper.pageToResponse(usersPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

}
