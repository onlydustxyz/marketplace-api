package onlydust.com.marketplace.api.domain.port.output;

import java.io.InputStream;

public interface ImageStoragePort {

    String storeImage(String fileName, InputStream imageInputStream);
}
