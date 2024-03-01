package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.Receipt;

public interface ReceiptStoragePort {
    void save(Receipt receipt);
}
