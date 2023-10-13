package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.MeApi;
import onlydust.com.marketplace.api.contract.model.GetMeResponse;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserMapper.userPayoutInformationToResponse;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserMapper.userToGetMeResponse;

@RestController
@Tags(@Tag(name = "USER"))
@AllArgsConstructor
public class MeRestApi implements MeApi {

    private final AuthenticationService authenticationService;
    private final UserFacadePort userFacadePort;

    @Override
    public ResponseEntity<GetMeResponse> getMyProfile() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final GetMeResponse getMeResponse = userToGetMeResponse(authenticatedUser);
        return ResponseEntity.ok(getMeResponse);

    }

    @Override
    public ResponseEntity<onlydust.com.marketplace.api.contract.model.UserPayoutInformation> getMyPayoutInfo() {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final UserPayoutInformation view = userFacadePort.getPayoutInformationForUserId(authenticatedUser.getId());
        final onlydust.com.marketplace.api.contract.model.UserPayoutInformation userPayoutInformation = userPayoutInformationToResponse(view);
        return ResponseEntity.ok(userPayoutInformation);
    }


}
