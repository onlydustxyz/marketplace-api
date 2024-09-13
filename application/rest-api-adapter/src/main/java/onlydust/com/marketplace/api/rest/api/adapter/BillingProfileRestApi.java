package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.CurrencyFacadePort;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.api.contract.BillingProfilesApi;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedAppUserService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BillingProfileMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.PayoutInfoMapper;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.SortDirectionMapper;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BillingProfileMapper.map;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Tags(@Tag(name = "BillingProfiles"))
@AllArgsConstructor
@Profile("api")
public class BillingProfileRestApi implements BillingProfilesApi {
    private final AuthenticatedAppUserService authenticatedAppUserService;
    private final BillingProfileFacadePort billingProfileFacadePort;
    private final CurrencyFacadePort currencyFacadePort;
    private final AccountingFacadePort accountingFacadePort;

    @Override
    public ResponseEntity<BillingProfileInvoicesPageResponse> getInvoices(UUID billingProfileId,
                                                                          Integer pageIndex,
                                                                          Integer pageSize,
                                                                          String sort,
                                                                          SortDirection direction) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var page = billingProfileFacadePort.invoicesOf(authenticatedUser.id(), BillingProfile.Id.of(billingProfileId), pageIndex,
                pageSize, map(sort), SortDirectionMapper.requestToDomain(direction));
        return ok(map(page, pageIndex));
    }

    @Override
    public ResponseEntity<InvoicePreviewResponse> previewNewInvoiceForRewardIds(UUID billingProfileId, List<UUID> rewardIds) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var preview = billingProfileFacadePort.previewInvoice(
                authenticatedUser.id(),
                BillingProfile.Id.of(billingProfileId),
                rewardIds.stream().map(RewardId::of).toList());

        final var usdToEurConversionRate = currencyFacadePort.latestQuote(Currency.Code.USD, Currency.Code.EUR);
        return ok(map(preview).usdToEurConversionRate(usdToEurConversionRate));
    }

    @Override
    public ResponseEntity<Void> uploadInvoice(UUID billingProfileId, UUID invoiceId, String fileName, Resource pdf) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        try {
            if (fileName != null && !fileName.trim().isEmpty()) {
                billingProfileFacadePort.uploadExternalInvoice(authenticatedUser.id(), BillingProfile.Id.of(billingProfileId),
                        Invoice.Id.of(invoiceId),
                        fileName.trim(), pdf.getInputStream());
            } else {
                billingProfileFacadePort.uploadGeneratedInvoice(authenticatedUser.id(), BillingProfile.Id.of(billingProfileId),
                        Invoice.Id.of(invoiceId),
                        pdf.getInputStream());
            }
        } catch (IOException e) {
            throw badRequest("Error while reading invoice data", e);
        }

        return noContent().build();
    }

    @Override
    public ResponseEntity<Resource> downloadInvoice(UUID billingProfileId, UUID invoiceId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var invoice = billingProfileFacadePort.downloadInvoice(
                authenticatedUser.id(),
                BillingProfile.Id.of(billingProfileId),
                Invoice.Id.of(invoiceId));

        return ok()
                .header("Content-Disposition", "attachment; filename=" + invoice.fileName())
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(invoice.data()));
    }

    @Override
    public ResponseEntity<Void> acceptOrDeclineInvoiceMandate(UUID billingProfileId, InvoiceMandateRequest invoiceMandateRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        if (Boolean.TRUE.equals(invoiceMandateRequest.getHasAcceptedInvoiceMandate())) {
            billingProfileFacadePort.acceptInvoiceMandate(authenticatedUser.id(), BillingProfile.Id.of(billingProfileId));
        }
        return noContent().build();
    }

    @Override
    public ResponseEntity<BillingProfileCreateResponse> createBillingProfile(BillingProfileRequest billingProfileRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final Set<ProjectId> projectIds = isNull(billingProfileRequest.getSelectForProjects()) ? Set.of() :
                billingProfileRequest.getSelectForProjects().stream().map(ProjectId::of).collect(Collectors.toSet());

        final var newBillingProfile = switch (billingProfileRequest.getType()) {
            case COMPANY -> billingProfileFacadePort.createCompanyBillingProfile(
                    authenticatedUser.id(),
                    billingProfileRequest.getName(),
                    projectIds);
            case SELF_EMPLOYED -> billingProfileFacadePort.createSelfEmployedBillingProfile(
                    authenticatedUser.id(),
                    billingProfileRequest.getName(),
                    projectIds);
            case INDIVIDUAL -> billingProfileFacadePort.createIndividualBillingProfile(
                    authenticatedUser.id(),
                    billingProfileRequest.getName(),
                    projectIds);
        };

        return ok(new BillingProfileCreateResponse().id(newBillingProfile.id().value()));
    }

    @Override
    public ResponseEntity<Void> setPayoutInfo(UUID billingProfileId, BillingProfilePayoutInfoRequest billingProfilePayoutInfoRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        billingProfileFacadePort.updatePayoutInfo(BillingProfile.Id.of(billingProfileId),
                authenticatedUser.id(),
                PayoutInfoMapper.mapToDomain(billingProfilePayoutInfoRequest));
        return noContent().build();
    }

    @Override
    public ResponseEntity<BillingProfileCoworkersPageResponse> getCoworkers(UUID billingProfileId, Integer pageIndex, Integer pageSize) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final Page<BillingProfileCoworkerView> coworkers = billingProfileFacadePort.getCoworkers(BillingProfile.Id.of(billingProfileId),
                authenticatedUser.id(), sanitizedPageIndex, sanitizedPageSize);
        return ok(BillingProfileMapper.coworkersPageToResponse(coworkers, pageIndex));
    }

    @Override
    public ResponseEntity<Void> inviteCoworker(UUID billingProfileId, BillingProfileCoworkerInvitationRequest invitationRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        billingProfileFacadePort.inviteCoworker(
                BillingProfile.Id.of(billingProfileId),
                authenticatedUser.id(),
                GithubUserId.of(invitationRequest.getGithubUserId()),
                switch (invitationRequest.getRole()) {
                    case ADMIN -> BillingProfile.User.Role.ADMIN;
                    case MEMBER -> BillingProfile.User.Role.MEMBER;
                });
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeCoworker(UUID billingProfileId, Long githubUserId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        billingProfileFacadePort.removeCoworker(
                BillingProfile.Id.of(billingProfileId),
                authenticatedUser.id(),
                GithubUserId.of(authenticatedUser.githubUserId()),
                GithubUserId.of(githubUserId));
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteBillingProfile(UUID billingProfileId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        billingProfileFacadePort.deleteBillingProfile(authenticatedUser.id(), BillingProfile.Id.of(billingProfileId));
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> enableBillingProfile(UUID billingProfileId, BillingProfileEnableRequest billingProfileEnableRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        billingProfileFacadePort.enableBillingProfile(authenticatedUser.id(), BillingProfile.Id.of(billingProfileId),
                billingProfileEnableRequest.getEnable());
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateBillingProfileType(UUID billingProfileId, BillingProfileTypeRequest billingProfileTypeRequest) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        billingProfileFacadePort.updateBillingProfileType(BillingProfile.Id.of(billingProfileId), authenticatedUser.id(),
                switch (billingProfileTypeRequest.getType()) {
                    case INDIVIDUAL -> BillingProfile.Type.INDIVIDUAL;
                    case COMPANY -> BillingProfile.Type.COMPANY;
                    case SELF_EMPLOYED -> BillingProfile.Type.SELF_EMPLOYED
                    ;
                });
        return noContent().build();
    }

    @Override
    public ResponseEntity<Void> updateCoworkerRole(UUID billingProfileId, Long githubUserId, UpdateCoworkerRoleRequest request) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();
        final var role = switch (request.getRole()) {
            case ADMIN -> BillingProfile.User.Role.ADMIN;
            case MEMBER -> BillingProfile.User.Role.MEMBER;
        };

        billingProfileFacadePort.updateCoworkerRole(BillingProfile.Id.of(billingProfileId), authenticatedUser.id(),
                GithubUserId.of(githubUserId), role);

        return noContent().build();
    }

    @Override
    public ResponseEntity<BillingProfileInvoiceableRewardsResponse> getInvoiceableRewards(UUID billingProfileId) {
        final var authenticatedUser = authenticatedAppUserService.getAuthenticatedUser();

        final var invoiceableRewards = billingProfileFacadePort.getInvoiceableRewardsForBillingProfile(authenticatedUser.id(),
                BillingProfile.Id.of(billingProfileId));

        final var rewardNetworks = invoiceableRewards.stream()
                .map(r -> Map.entry(r.getId(), accountingFacadePort.networksOf(r.getAmount().getCurrency().id(), RewardId.of(r.getId()))))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        return ok(BillingProfileMapper.mapToInvoiceableRewardsResponse(invoiceableRewards, rewardNetworks, authenticatedUser));
    }
}
