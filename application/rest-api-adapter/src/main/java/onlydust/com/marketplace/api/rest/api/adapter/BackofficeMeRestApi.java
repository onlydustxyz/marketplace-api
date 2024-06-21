package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeMeApi;
import onlydust.com.backoffice.api.contract.model.BackofficeUserRole;
import onlydust.com.backoffice.api.contract.model.MeResponse;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedBackofficeUserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "BackofficeMe"))
@AllArgsConstructor
@Profile("bo")
public class BackofficeMeRestApi implements BackofficeMeApi {

    private final AuthenticatedBackofficeUserService authenticatedBackofficeUserService;

    @Override
    public ResponseEntity<MeResponse> getMe() {
        final var user = authenticatedBackofficeUserService.getAuthenticatedBackofficeUser();
        return ResponseEntity.ok(new MeResponse()
                .avatarUrl(user.avatarUrl())
                .name(user.name())
                .email(user.email())
                .roles(user.roles().stream().map(role -> switch (role) {
                            case BO_MARKETING_ADMIN -> BackofficeUserRole.MARKETING_ADMIN;
                            case BO_FINANCIAL_ADMIN -> BackofficeUserRole.FINANCIAL_ADMIN;
                            case BO_READER -> BackofficeUserRole.READER;
                        }
                ).toList()));
    }
}
