package onlydust.com.marketplace.bff.read.adapters;

import io.github.perplexhub.rsql.RSQLJPASupport;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import onlydust.com.backoffice.api.contract.BackofficeUsersReadApi;
import onlydust.com.backoffice.api.contract.model.UserDetailsResponse;
import onlydust.com.backoffice.api.contract.model.UserPage;
import onlydust.com.backoffice.api.contract.model.UserSearchPage;
import onlydust.com.marketplace.bff.read.entities.user.rsql.AllUserRSQLEntity;
import onlydust.com.marketplace.bff.read.mapper.UserMapper;
import onlydust.com.marketplace.bff.read.repositories.AllUserRSQLRepository;
import onlydust.com.marketplace.bff.read.repositories.UserReadRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.*;

@RestController
@AllArgsConstructor
@Transactional(readOnly = true)
public class BackofficeUsersReadApiPostgresAdapter implements BackofficeUsersReadApi {

    private UserReadRepository userReadRepository;
    private AllUserRSQLRepository allUserRSQLRepository;

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

    //(contributionLanguage:name==Rust and contributionLanguage:count>20) or (contribution-language:name==Java and contribution-language:count>100) and (rank-global:value<100 or (rank-language:name==Rust and rank-language:value<30) or (rank-language:name==Java and rank-language:value<10))
    @SneakyThrows
    @Override
    public ResponseEntity<UserSearchPage> searchUsers(Integer pageIndex, Integer pageSize, String query) {
        final Map<String, String> propertyMapping = new HashMap<>();
        propertyMapping.put("language.id", "language.language.id");
        propertyMapping.put("language.name", "language.language.name");
        propertyMapping.put("ecosystem.id", "ecosystem.ecosystem.id");
        propertyMapping.put("ecosystem.name", "ecosystem.ecosystem.name");

        final var page = allUserRSQLRepository.findAll(RSQLJPASupport.toSpecification(query, propertyMapping), PageRequest.of(pageIndex, pageSize));

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
