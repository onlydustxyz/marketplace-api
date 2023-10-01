package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.MeApi;
import onlydust.com.marketplace.api.contract.model.GetMeResponse;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Me"))
@AllArgsConstructor
public class MeRestApi implements MeApi {

    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<GetMeResponse> getMyProfile() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final GetMeResponse getMeResponse = new GetMeResponse();
        getMeResponse.setId(authenticatedUser.getId().toString());
        getMeResponse.setGithubUserId(authenticatedUser.getGithubUserId());
        getMeResponse.setAvatarUrl(authenticatedUser.getAvatarUrl());
        getMeResponse.setLogin(authenticatedUser.getLogin());
        return ResponseEntity.ok(getMeResponse);
    }
}
