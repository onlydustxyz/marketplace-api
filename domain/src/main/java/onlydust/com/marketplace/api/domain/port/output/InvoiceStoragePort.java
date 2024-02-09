package onlydust.com.marketplace.api.domain.port.output;

import java.io.InputStream;
import java.net.URL;

public interface InvoiceStoragePort {

    URL storePdfForName(String name, InputStream imageInputStream);
}
