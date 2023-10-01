package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.MeApi;
import onlydust.com.marketplace.api.contract.model.GetMeResponse;
import onlydust.com.marketplace.api.domain.exception.OnlydustException;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.exception.RestApiExceptionCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserMapper.userToGetMeResponse;

@RestController
@Tags(@Tag(name = "Me"))
@AllArgsConstructor
public class MeRestApi implements MeApi {

    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<GetMeResponse> getMyProfile() {
        try {
            final User authenticatedUser = authenticationService.getAuthenticatedUser();
            final GetMeResponse getMeResponse = userToGetMeResponse(authenticatedUser);
            return ResponseEntity.ok(getMeResponse);
        } catch (OnlydustException onlydustException) {
            if (onlydustException.getCode().equals(RestApiExceptionCode.UNAUTHORIZED)) {
                return ResponseEntity.status(401).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }


}
