package onlydust.com.marketplace.kernel.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.Event;

public interface MailPort {

    void send(@NonNull Event event);
}
