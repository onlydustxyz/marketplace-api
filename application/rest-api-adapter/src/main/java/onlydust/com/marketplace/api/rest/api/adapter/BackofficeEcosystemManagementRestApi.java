package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeEcosystemManagementApi;
import onlydust.com.backoffice.api.contract.model.EcosystemCreateResponse;
import onlydust.com.backoffice.api.contract.model.EcosystemRequest;
import onlydust.com.backoffice.api.contract.model.UploadImageResponse;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.port.input.EcosystemFacadePort;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "BackofficeEcosystemManagement"))
@AllArgsConstructor
@Profile("bo")
public class BackofficeEcosystemManagementRestApi implements BackofficeEcosystemManagementApi {
    private final EcosystemFacadePort ecosystemFacadePort;

    @Override
    public ResponseEntity<EcosystemCreateResponse> createEcosystem(EcosystemRequest request) {
        final var ecosystem = ecosystemFacadePort.createEcosystem(request.getName(),
                request.getUrl(),
                request.getLogoUrl(),
                request.getDescription(),
                request.getHidden(),
                request.getLeads().stream().map(UserId::of).toList());

        return ok(new EcosystemCreateResponse()
                .id(ecosystem.id()));
    }

    @Override
    public ResponseEntity<Void> updateEcosystem(UUID ecosystemId, EcosystemRequest request) {
        ecosystemFacadePort.updateEcosystem(ecosystemId,
                request.getName(),
                request.getUrl(),
                request.getLogoUrl(),
                request.getDescription(),
                request.getHidden(),
                request.getLeads().stream().map(UserId::of).toList());

        return noContent().build();
    }

    @Override
    public ResponseEntity<UploadImageResponse> uploadEcosystemLogo(Resource image) {
        InputStream imageInputStream;
        try {
            imageInputStream = image.getInputStream();
        } catch (IOException e) {
            throw badRequest("Error while reading image data", e);
        }

        final URL imageUrl = ecosystemFacadePort.uploadLogo(imageInputStream);
        final var response = new UploadImageResponse().url(imageUrl.toString());

        return ok(response);
    }
}
