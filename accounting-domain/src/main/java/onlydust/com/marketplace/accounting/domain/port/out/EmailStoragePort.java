package onlydust.com.marketplace.accounting.domain.port.out;

import lombok.NonNull;

public interface EmailStoragePort {

    void send(@NonNull String email, @NonNull Object object);
}
