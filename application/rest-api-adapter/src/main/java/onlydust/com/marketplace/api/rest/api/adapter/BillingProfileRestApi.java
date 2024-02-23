package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.api.contract.BillingProfilesApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BillingProfileMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.PayoutInfoMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.SortDirectionMapper;
import onlydust.com.marketplace.project.domain.model.User;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BillingProfileMapper.map;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "BillingProfile"))
@AllArgsConstructor
public class BillingProfileRestApi implements BillingProfilesApi {
    private final AuthenticationService authenticationService;
    private final BillingProfileFacadePort billingProfileFacadePort;
    private final CurrencyFacadePort currencyFacadePort;

    @Override
    public ResponseEntity<BillingProfileInvoicesPageResponse> getInvoices(UUID billingProfileId,
                                                                          Integer pageIndex,
                                                                          Integer pageSize,
                                                                          String sort,
                                                                          String direction) {
        final var authenticatedUser = authenticationService.getAuthenticatedUser();
        final var page = billingProfileFacadePort.invoicesOf(UserId.of(authenticatedUser.getId()), BillingProfile.Id.of(billingProfileId), pageIndex,
                pageSize, map(sort), SortDirectionMapper.requestToDomain(direction));
        return ok(map(page, pageIndex));
    }

    @Override
    public ResponseEntity<InvoicePreviewResponse> previewNewInvoiceForRewardIds(UUID billingProfileId, List<UUID> rewardIds) {
        final var authenticatedUser = authenticationService.getAuthenticatedUser();
        final var preview = billingProfileFacadePort.previewInvoice(
                UserId.of(authenticatedUser.getId()),
                BillingProfile.Id.of(billingProfileId),
                rewardIds.stream().map(RewardId::of).toList());

        final var usdToEurConversionRate = currencyFacadePort.latestQuote(Currency.Code.USD, Currency.Code.EUR);
        return ok(map(preview).usdToEurConversionRate(usdToEurConversionRate));
    }

    @Override
    public ResponseEntity<Void> uploadInvoice(UUID billingProfileId, UUID invoiceId, String fileName, Resource pdf) {
        final var authenticatedUser = authenticationService.getAuthenticatedUser();

        try {
            if (fileName != null && !fileName.trim().isEmpty()) {
                billingProfileFacadePort.uploadExternalInvoice(UserId.of(authenticatedUser.getId()), BillingProfile.Id.of(billingProfileId),
                        Invoice.Id.of(invoiceId),
                        fileName.trim(), pdf.getInputStream());
            } else {
                billingProfileFacadePort.uploadGeneratedInvoice(UserId.of(authenticatedUser.getId()), BillingProfile.Id.of(billingProfileId),
                        Invoice.Id.of(invoiceId),
                        pdf.getInputStream());
            }
        } catch (IOException e) {
            throw badRequest("Error while reading invoice data", e);
        }

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Resource> downloadInvoice(UUID billingProfileId, UUID invoiceId) {
        final var authenticatedUser = authenticationService.getAuthenticatedUser();
        final var invoice = billingProfileFacadePort.downloadInvoice(
                UserId.of(authenticatedUser.getId()),
                BillingProfile.Id.of(billingProfileId),
                Invoice.Id.of(invoiceId));

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + invoice.fileName())
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(invoice.data()));
    }

    @Override
    public ResponseEntity<Void> acceptOrDeclineInvoiceMandate(UUID billingProfileId, InvoiceMandateRequest invoiceMandateRequest) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        if (Boolean.TRUE.equals(invoiceMandateRequest.getHasAcceptedInvoiceMandate())) {
            billingProfileFacadePort.updateInvoiceMandateAcceptanceDate(UserId.of(authenticatedUser.getId()), BillingProfile.Id.of(billingProfileId));
        }
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BillingProfileResponse> createBillingProfile(BillingProfileRequest billingProfileRequest) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final Set<ProjectId> projectIds = isNull(billingProfileRequest.getSelectForProjects()) ? Set.of() :
                billingProfileRequest.getSelectForProjects().stream().map(ProjectId::of).collect(Collectors.toSet());
        final BillingProfileResponse billingProfileResponse = BillingProfileMapper.billingProfileToResponse(switch (billingProfileRequest.getType()) {
            case COMPANY -> billingProfileFacadePort.createCompanyBillingProfile(UserId.of(authenticatedUser.getId()), billingProfileRequest.getName(),
                    projectIds);
            case SELF_EMPLOYED ->
                    billingProfileFacadePort.createSelfEmployedBillingProfile(UserId.of(authenticatedUser.getId()), billingProfileRequest.getName(),
                            projectIds);
            case INDIVIDUAL -> billingProfileFacadePort.createIndividualBillingProfile(UserId.of(authenticatedUser.getId()), billingProfileRequest.getName(),
                    projectIds);
        });
        return ResponseEntity.ok(billingProfileResponse);
    }

    @Override
    public ResponseEntity<BillingProfileResponse> getBillingProfile(UUID billingProfileId) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        final BillingProfileView billingProfileView = billingProfileFacadePort.getBillingProfile(BillingProfile.Id.of(billingProfileId),
                UserId.of(authenticatedUser.getId()));
        return ResponseEntity.ok(BillingProfileMapper.billingProfileViewToResponse(billingProfileView));
    }

    @Override
    public ResponseEntity<BillingProfilePayoutInfoResponse> getPayoutInfo(UUID billingProfileId) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        PayoutInfo payoutInfo = billingProfileFacadePort.getPayoutInfo(BillingProfile.Id.of(billingProfileId), UserId.of(authenticatedUser.getId()));
        return ResponseEntity.ok(PayoutInfoMapper.mapToResponse(payoutInfo));
    }

    @Override
    public ResponseEntity<Void> setPayoutInfo(UUID billingProfileId, BillingProfilePayoutInfoRequest billingProfilePayoutInfoRequest) {
        final User authenticatedUser = authenticationService.getAuthenticatedUser();
        billingProfileFacadePort.updatePayoutInfo(BillingProfile.Id.of(billingProfileId), UserId.of(authenticatedUser.getId()),
                PayoutInfoMapper.mapToDomain(billingProfilePayoutInfoRequest));
        return ResponseEntity.ok().build();
    }
}
