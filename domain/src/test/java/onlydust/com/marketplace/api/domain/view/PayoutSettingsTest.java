package onlydust.com.marketplace.api.domain.view;

import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PayoutSettingsTest {

    @Test
    void should_get_is_valid() {
        Assertions.assertEquals(false, UserPayoutInformation.PayoutSettings.builder()
                .hasMissingAptosWallet(true)
                .hasMissingEthWallet(false)
                .hasMissingStarknetWallet(false)
                .hasMissingOptimismWallet(false)
                .hasMissingBankingAccount(false)
                .build().isValid());
        Assertions.assertEquals(true, UserPayoutInformation.PayoutSettings.builder()
                .hasMissingAptosWallet(false)
                .hasMissingEthWallet(false)
                .hasMissingStarknetWallet(false)
                .hasMissingOptimismWallet(false)
                .hasMissingBankingAccount(false)
                .build().isValid());
    }
}
