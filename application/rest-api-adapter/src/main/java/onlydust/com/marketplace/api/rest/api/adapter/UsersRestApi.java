package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.UsersApi;
import onlydust.com.marketplace.api.contract.model.PublicUserProfileResponse;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserMapper.userProfileToPublicResponse;


@RestController
@Tags(@Tag(name = "Users"))
@AllArgsConstructor
public class UsersRestApi implements UsersApi {

    private final UserFacadePort userFacadePort;

    @Override
    public ResponseEntity<PublicUserProfileResponse> getUserProfile(Long githubId) {
        final UserProfileView userProfileView = userFacadePort.getProfileById(githubId);
        final PublicUserProfileResponse userProfileResponse = userProfileToPublicResponse(userProfileView);
        return ResponseEntity.ok(userProfileResponse);
    }
}
