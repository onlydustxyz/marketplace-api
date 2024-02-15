package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.api.contract.BillingProfilesApi;
import onlydust.com.marketplace.api.contract.model.BillingProfileInvoicesPageResponse;
import onlydust.com.marketplace.api.contract.model.NewInvoiceResponse;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BillingProfileMapper.map;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "BillingProfile"))
@AllArgsConstructor
public class BillingProfileRestApi implements BillingProfilesApi {
    private final AuthenticationService authenticationService;
    private final BillingProfileFacadePort billingProfileFacadePort;

    @Override
    public ResponseEntity<BillingProfileInvoicesPageResponse> getInvoices(UUID billingProfileId, Integer pageIndex, Integer pageSize) {
        final var authenticatedUser = authenticationService.getAuthenticatedUser();
        final var page = billingProfileFacadePort.getInvoicesForBillingProfile(UserId.of(authenticatedUser.getId()), BillingProfile.Id.of(billingProfileId));
        return ok(map(page, pageIndex));
    }

    @Override
    public ResponseEntity<NewInvoiceResponse> previewNewInvoiceForRewardIds(UUID billingProfileId, List<UUID> rewardIds) {
        final var authenticatedUser = authenticationService.getAuthenticatedUser();
        final var preview = billingProfileFacadePort.previewInvoice(
                UserId.of(authenticatedUser.getId()),
                BillingProfile.Id.of(billingProfileId),
                rewardIds.stream().map(RewardId::of).toList());

        return ok(map(preview));
    }

//    @Override
//    public ResponseEntity<Void> uploadInvoice(String filename, Resource pdf) {
//        final User authenticatedUser = authenticationService.getAuthenticatedUser();
//        InputStream pdfInputStream;
//        try {
//            pdfInputStream = pdf.getInputStream();
//        } catch (IOException e) {
//            throw OnlyDustException.badRequest("Error while reading image data", e);
//        }
//
//        final URL pdfUrl = userFacadePort.saveInvoicePdfForGithubUserId(authenticatedUser.getGithubUserId(), pdfInputStream);
//        final UploadPdfResponse response = new UploadPdfResponse();
//        response.url(pdfUrl.toString());
//        return ResponseEntity.ok().build();
//    }
}
