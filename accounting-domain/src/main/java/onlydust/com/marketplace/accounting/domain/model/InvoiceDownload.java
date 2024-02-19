package onlydust.com.marketplace.accounting.domain.model;

import java.io.InputStream;

public record InvoiceDownload(InputStream data, String fileName) {
}
