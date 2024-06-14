package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.TechnologiesApi;
import onlydust.com.marketplace.api.contract.model.AllTechnologiesResponse;
import onlydust.com.marketplace.project.domain.port.input.TechnologiesPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Technologies"))
@AllArgsConstructor
public class TechnologiesRestApi implements TechnologiesApi {
    private final TechnologiesPort technologiesPort;

    @Override
    public ResponseEntity<AllTechnologiesResponse> getTechnologies() {
        final var technologies = technologiesPort.getAllUsedTechnologies();
        return new ResponseEntity<>(new AllTechnologiesResponse().technologies(technologies), HttpStatus.OK);
    }
}
