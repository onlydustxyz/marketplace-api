package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.TechnologiesApi;
import onlydust.com.marketplace.api.contract.model.AllTechnologiesResponse;
import onlydust.com.marketplace.api.contract.model.SuggestTechnologyRequest;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.TechnologiesPort;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Technologies"))
@AllArgsConstructor
public class TechnologiesRestApi implements TechnologiesApi {
    private final TechnologiesPort technologiesPort;
    private final AuthenticationService authenticationService;

    @Override
    public ResponseEntity<AllTechnologiesResponse> getTechnologies() {
        final var technologies = technologiesPort.getAllUsedTechnologies();
        return new ResponseEntity<>(new AllTechnologiesResponse().technologies(technologies), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> suggestTechnology(SuggestTechnologyRequest request) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();

        technologiesPort.suggest(request.getTechnology(), authenticatedUser);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
