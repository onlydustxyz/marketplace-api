package onlydust.com.marketplace.api.domain.port.output;

import java.io.InputStream;
import java.net.URL;

public interface ImageStoragePort {

  URL storeImage(InputStream imageInputStream);
}
