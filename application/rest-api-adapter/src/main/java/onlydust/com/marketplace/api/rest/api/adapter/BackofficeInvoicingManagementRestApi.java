package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeInvoicingManagementApi;
import onlydust.com.backoffice.api.contract.model.InvoiceInternalStatus;
import onlydust.com.backoffice.api.contract.model.InvoicePage;
import onlydust.com.backoffice.api.contract.model.PatchInvoiceRequest;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.port.in.InvoiceFacadePort;
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

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapInvoicePageToContract;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapInvoiceStatus;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "BackofficeInvoicingManagement"))
@AllArgsConstructor
public class BackofficeInvoicingManagementRestApi implements BackofficeInvoicingManagementApi {
    private final InvoiceFacadePort invoiceFacadePort;
    private final QueryParamTokenAuthenticationService.Config queryParamTokenAuthenticationConfig;
    final static Integer MAX_PAGE_SIZE = Integer.MAX_VALUE;
    final static List<InvoiceInternalStatus> ALL_STATUSES = List.of(InvoiceInternalStatus.values());

    @Override
    public ResponseEntity<InvoicePage> getInvoicePage(Integer pageIndex, Integer pageSize, List<UUID> invoiceIds,
                                                      List<InvoiceInternalStatus> internalStatuses) {
        final int sanitizedPageSize = sanitizePageSize(pageSize, MAX_PAGE_SIZE);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var page = invoiceFacadePort.findAll(
                Optional.ofNullable(invoiceIds).orElse(List.of()).stream().map(Invoice.Id::of).toList(),
                Optional.ofNullable(internalStatuses).orElse(ALL_STATUSES).stream().map(BackOfficeMapper::mapInvoiceStatus).toList(),
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
    public ResponseEntity<Void> updateInvoice(UUID invoiceId, PatchInvoiceRequest request) {
        invoiceFacadePort.update(Invoice.Id.of(invoiceId), mapInvoiceStatus(request.getStatus()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Resource> downloadInvoice(UUID invoiceId) {
        final var invoice = invoiceFacadePort.download(Invoice.Id.of(invoiceId));

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + invoice.fileName())
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(invoice.data()));
    }
}
