package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeSponsorManagementApi;
import onlydust.com.backoffice.api.contract.model.SponsorCreateResponse;
import onlydust.com.backoffice.api.contract.model.SponsorRequest;
import onlydust.com.backoffice.api.contract.model.UploadImageResponse;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.port.input.SponsorFacadePort;
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

@RestController
@Tags(@Tag(name = "BackofficeSponsorManagement"))
@AllArgsConstructor
@Profile("bo")
public class BackofficeSponsorManagementRestApi implements BackofficeSponsorManagementApi {

    private final SponsorFacadePort sponsorFacadePort;

    @Override
    public ResponseEntity<SponsorCreateResponse> createSponsor(SponsorRequest request) {
        final var sponsor = sponsorFacadePort.createSponsor(request.getName(),
                request.getUrl(),
                request.getLogoUrl(),
                request.getLeads().stream().map(UserId::of).toList());
        return ResponseEntity.ok(new SponsorCreateResponse()
                .id(sponsor.id().value()));
    }

    @Override
    public ResponseEntity<Void> updateSponsor(UUID sponsorId, SponsorRequest request) {
        sponsorFacadePort.updateSponsor(SponsorId.of(sponsorId),
                request.getName(),
                request.getUrl(),
                request.getLogoUrl(),
                request.getLeads().stream().map(UserId::of).toList());
        return noContent().build();
    }

    @Override
    public ResponseEntity<UploadImageResponse> uploadSponsorLogo(Resource image) {
        InputStream imageInputStream;
        try {
            imageInputStream = image.getInputStream();
        } catch (IOException e) {
            throw badRequest("Error while reading image data", e);
        }

        final URL imageUrl = sponsorFacadePort.uploadLogo(imageInputStream);
        final var response = new UploadImageResponse().url(imageUrl.toString());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> addLeadToSponsor(UUID sponsorId, UUID leadId) {
        sponsorFacadePort.addLeadToSponsor(leadId, SponsorId.of(sponsorId));
        return noContent().build();
    }
}
