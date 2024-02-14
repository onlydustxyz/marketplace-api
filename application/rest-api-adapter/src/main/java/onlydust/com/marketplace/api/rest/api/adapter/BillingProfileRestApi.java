package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.api.contract.BillingProfilesApi;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "BillingProfile"))
@AllArgsConstructor
public class BillingProfileRestApi implements BillingProfilesApi {

    private final AuthenticationService authenticationService;
    private final BillingProfileFacadePort billingProfileFacadePort;



//    @Override
//    public ResponseEntity<NewInvoiceResponse> previewNewInvoiceForRewardIds(List<UUID> rewardIds) {
//        final User authenticatedUser = authenticationService.getAuthenticatedUser();
//        InvoicePreviewView invoicePreviewView = invoiceFacadePort.generateNextInvoicePreviewForUserAndRewards(authenticatedUser.getId(), rewardIds);
//        return MeApi.super.previewNewInvoiceForRewardIds(rewardIds);
//    }

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
