package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.TechnologiesApi;
import onlydust.com.marketplace.api.contract.model.SuggestTechnologyRequest;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.TechnologiesPort;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Tags(@Tag(name = "Technologies"))
@AllArgsConstructor
public class TechnologiesRestApi implements TechnologiesApi {
    private final TechnologiesPort technologiesPort;
    private final AuthenticationService authenticationService;

    public ResponseEntity<Void> suggestTechnology(SuggestTechnologyRequest request) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();

        technologiesPort.suggest(request.getTechnology(), authenticatedUser);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
