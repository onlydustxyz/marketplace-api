package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;

import java.io.InputStream;
import java.net.URL;

public interface PdfStoragePort {
    URL upload(final @NonNull String fileName, final @NonNull InputStream data);
}
