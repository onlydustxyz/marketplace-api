package onlydust.com.marketplace.kernel.port.output;

import java.io.InputStream;
import java.net.URL;

public interface ImageStoragePort {

    URL storeImage(InputStream imageInputStream);
}
