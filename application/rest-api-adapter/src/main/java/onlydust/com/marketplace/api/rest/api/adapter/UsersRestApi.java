package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.UsersApi;
import onlydust.com.marketplace.api.contract.model.UserProfileResponse;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserMapper.userProfileToResponse;


@RestController
@Tags(@Tag(name = "Users"))
@AllArgsConstructor
public class UsersRestApi implements UsersApi {

    private final UserFacadePort userFacadePort;

    @Override
    public ResponseEntity<UserProfileResponse> getUserProfile(UUID userId) {
        final UserProfileView userProfileView = userFacadePort.getProfileById(userId);
        final UserProfileResponse userProfileResponse = userProfileToResponse(userProfileView);
        return ResponseEntity.ok(userProfileResponse);
    }


}
