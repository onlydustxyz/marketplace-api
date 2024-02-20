package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeInvoicingManagementApi;
import onlydust.com.backoffice.api.contract.model.InvoicePage;
import onlydust.com.backoffice.api.contract.model.PatchInvoiceRequest;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.port.in.InvoiceFacadePort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapInvoicePageToContract;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.BackOfficeMapper.mapInvoiceStatus;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageIndex;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.sanitizePageSize;

@RestController
@Tags(@Tag(name = "BackofficeCurrencyManagement"))
@AllArgsConstructor
public class BackofficeInvoicingManagementRestApi implements BackofficeInvoicingManagementApi {
    private final InvoiceFacadePort invoiceFacadePort;
    final static Integer MAX_PAGE_SIZE = Integer.MAX_VALUE;

    @Override
    public ResponseEntity<InvoicePage> getInvoicePage(Integer pageIndex, Integer pageSize) {
        final int sanitizedPageSize = sanitizePageSize(pageSize, MAX_PAGE_SIZE);
        final int sanitizedPageIndex = sanitizePageIndex(pageIndex);
        final var page = invoiceFacadePort.findAllExceptDrafts(sanitizedPageIndex, sanitizedPageSize);

        final var response = mapInvoicePageToContract(page, pageIndex);

        return page.getTotalPageNumber() > 1 ?
                ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response) :
                ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> updateInvoice(UUID invoiceId, PatchInvoiceRequest request) {
        invoiceFacadePort.update(Invoice.Id.of(invoiceId), mapInvoiceStatus(request.getStatus()));
        return ResponseEntity.noContent().build();
    }
}
