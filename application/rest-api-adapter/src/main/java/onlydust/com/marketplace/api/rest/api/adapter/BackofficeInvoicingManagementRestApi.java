package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeInvoicingManagementApi;
import onlydust.com.backoffice.api.contract.model.*;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.InvoiceFacadePort;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticatedBackofficeUserService;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.token.QueryParamTokenAuthenticationService;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.*;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "BackofficeInvoicingManagement"))
@AllArgsConstructor
public class BackofficeInvoicingManagementRestApi implements BackofficeInvoicingManagementApi {
    private final InvoiceFacadePort invoiceFacadePort;
    private final QueryParamTokenAuthenticationService.Config queryParamTokenAuthenticationConfig;
    private final AuthenticatedBackofficeUserService authenticatedBackofficeUserService;
    final static Integer MAX_PAGE_SIZE = Integer.MAX_VALUE;

    @Override
    public ResponseEntity<InvoicePage> getInvoicePage(Integer pageIndex, Integer pageSize, List<UUID> invoiceIds,
                                                      List<InvoiceInternalStatus> internalStatuses) {
        final int sanitizedPageSize = sanitizePageSize(pageSize, MAX_PAGE_SIZE);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var page = invoiceFacadePort.findAll(
                Optional.ofNullable(invoiceIds).orElse(List.of()).stream().map(Invoice.Id::of).toList(),
                Optional.ofNullable(internalStatuses).orElse(List.of()).stream().map(BackOfficeMapper::mapInvoiceStatus).toList(),
                List.of(),
                List.of(),
                List.of(),
                null,
                sanitizedPageIndex,
                sanitizedPageSize
        );

        final var response = mapInvoicePageToContract(page, pageIndex, queryParamTokenAuthenticationConfig.getBaseUrl(),
                queryParamTokenAuthenticationConfig.getToken());

        return page.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<InvoicePageV2> getInvoicePageV2(Integer pageIndex,
                                                          Integer pageSize,
                                                          List<UUID> invoiceIds,
                                                          List<InvoiceInternalStatus> statuses,
                                                          List<UUID> currencies,
                                                          List<BillingProfileType> billingProfileTypes,
                                                          List<UUID> billingProfiles,
                                                          String search) {
        final int sanitizedPageSize = sanitizePageSize(pageSize);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var page = invoiceFacadePort.findAll(
                Optional.ofNullable(invoiceIds).orElse(List.of()).stream().map(Invoice.Id::of).toList(),
                Optional.ofNullable(statuses).orElse(List.of()).stream().map(BackOfficeMapper::mapInvoiceStatus).toList(),
                Optional.ofNullable(currencies).orElse(List.of()).stream().map(Currency.Id::of).toList(),
                Optional.ofNullable(billingProfileTypes).orElse(List.of()).stream().map(BackOfficeMapper::map).toList(),
                Optional.ofNullable(billingProfiles).orElse(List.of()).stream().map(BillingProfile.Id::of).toList(),
                search,
                sanitizedPageIndex,
                sanitizedPageSize
        );

        final var response = mapInvoicePageV2ToContract(page, pageIndex);

        return page.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<InvoiceDetailsResponse> getInvoice(UUID invoiceId) {
        final var authenticatedUser = authenticatedBackofficeUserService.getAuthenticatedBackofficeUser().asAuthenticatedUser();

        final var invoice = invoiceFacadePort.find(Invoice.Id.of(invoiceId))
                .orElseThrow(() -> notFound("Invoice %s not found".formatted(invoiceId)));

        final var response = mapInvoiceToContract(invoice, authenticatedUser);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Resource> downloadInvoice(UUID invoiceId) {
        final var invoice = invoiceFacadePort.download(Invoice.Id.of(invoiceId));

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + invoice.fileName())
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(invoice.data()));
    }

    @Override
    public ResponseEntity<Void> updateInvoiceStatus(UUID invoiceId, UpdateInvoiceStatusRequest updateInvoiceStatusRequest) {
        invoiceFacadePort.update(Invoice.Id.of(invoiceId), mapInvoiceStatus(updateInvoiceStatusRequest.getStatus()),
                updateInvoiceStatusRequest.getRejectionReason());
        return ResponseEntity.noContent().build();
    }
}
