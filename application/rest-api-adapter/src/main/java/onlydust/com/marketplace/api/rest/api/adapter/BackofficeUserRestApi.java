package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeUserApi;
import onlydust.com.backoffice.api.contract.model.UserPage;
import onlydust.com.marketplace.project.domain.port.input.BackofficeFacadePort;
import onlydust.com.marketplace.project.domain.view.backoffice.UserShortView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapUserPageToContract;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "BackofficeUser"))
@AllArgsConstructor
public class BackofficeUserRestApi implements BackofficeUserApi {
    private final BackofficeFacadePort backofficeFacadePort;
    final static Integer MAX_PAGE_SIZE = Integer.MAX_VALUE;

    @Override
    public ResponseEntity<UserPage> getUserPage(Integer pageIndex, Integer pageSize, String login) {
        final var sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var filters = UserShortView.Filters.builder()
                .loginLike(login)
                .build();
        final var usersPage = backofficeFacadePort.listUsers(sanitizedPageIndex, sanitizePageSize(pageSize, MAX_PAGE_SIZE), filters);
        final var response = mapUserPageToContract(usersPage, sanitizedPageIndex);

        return response.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

}
