package onlydust.com.marketplace.api.read.adapters;

import io.github.perplexhub.rsql.RSQLException;
import io.github.perplexhub.rsql.RSQLJPASupport;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import onlydust.com.backoffice.api.contract.BackofficeUsersReadApi;
import onlydust.com.backoffice.api.contract.model.UserDetailsResponse;
import onlydust.com.backoffice.api.contract.model.UserPage;
import onlydust.com.backoffice.api.contract.model.UserSearchPage;
import onlydust.com.marketplace.api.read.entities.user.rsql.AllUserRSQLEntity;
import onlydust.com.marketplace.api.read.mapper.UserMapper;
import onlydust.com.marketplace.api.read.repositories.AllUserRSQLRepository;
import onlydust.com.marketplace.api.read.repositories.UserReadRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.github.perplexhub.rsql.RSQLJPASupport.toSort;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
@Profile("bo")
public class BackofficeUsersReadApiPostgresAdapter implements BackofficeUsersReadApi {

    private UserReadRepository userReadRepository;
    private AllUserRSQLRepository allUserRSQLRepository;

    @Override
    public ResponseEntity<UserDetailsResponse> getUserById(UUID userId) {
        final var user = userReadRepository.findBoUser(userId)
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

    // global.rank<100 and (language.name==Rust and language.rank<20 or ecosystem.name==Starknet and ecosystem.rank<20)
    @SneakyThrows
    @Override
    public ResponseEntity<UserSearchPage> searchUsers(Integer pageIndex, Integer pageSize, String query, String sort) {
        final Map<String, String> propertyMapping = new HashMap<>();
        propertyMapping.put("language.id", "language.language.id");
        propertyMapping.put("language.name", "language.language.name");
        propertyMapping.put("ecosystem.id", "ecosystem.ecosystem.id");
        propertyMapping.put("ecosystem.name", "ecosystem.ecosystem.name");

        Page<AllUserRSQLEntity> page;
        try {
            page = allUserRSQLRepository.findAll(
                    RSQLJPASupport.<AllUserRSQLEntity>toSpecification(query, propertyMapping).and(toSort(sort, propertyMapping)),
                    PageRequest.of(pageIndex, pageSize));
        } catch (RSQLException e) {
            throw badRequest("Invalid query: '%s' and/or sort: '%s' (%s error)"
                    .formatted(query, sort, e.getClass().getSimpleName().replaceAll("Exception", "")));
        }

        final var response = new UserSearchPage()
                .users(page.getContent().stream().map(AllUserRSQLEntity::toBoPageItemResponse).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));
        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }
}