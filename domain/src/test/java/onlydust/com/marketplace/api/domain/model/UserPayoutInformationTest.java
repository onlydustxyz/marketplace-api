package onlydust.com.marketplace.api.domain.model;

import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserPayoutInformationTest {

    @Test
    void validate_aptos() {
        assertDoesNotThrow(() ->
                UserPayoutInformation.builder()
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .aptosAddress("0xeeff357ea5c1a4e7bc11b2b17ff2dc2dcca69750bfef1e1ebcaccf8c8018175b")
                                .build())
                        .build().validateWalletAddresses());

        assertDoesNotThrow(() ->
                UserPayoutInformation.builder()
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .aptosAddress("0x0000357ea5c1a4e7bc11b2b17ff2dc2dcca69750bfef1e1ebcaccf8c8018175b")
                                .build())
                        .build().validateWalletAddresses());
    }

    @Test
    void validate_aptos_invalid() {
        assertThrows(OnlyDustException.class, () ->
                UserPayoutInformation.builder()
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .aptosAddress("0x1234567890123456789012345678901234567890")
                                .build())
                        .build().validateWalletAddresses());
    }

    @Test
    void validate_eth() {
        assertDoesNotThrow(() ->
                UserPayoutInformation.builder()
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .ethAddress("0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045")
                                .build())
                        .build().validateWalletAddresses());
    }

    @Test
    void validate_eth_invalid() {
        assertThrows(OnlyDustException.class, () ->
                UserPayoutInformation.builder()
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .ethAddress("0x000d8dA6BF26964aF9D7eEd9e03E53415D37aA96045")
                                .build())
                        .build().validateWalletAddresses());
    }

    @Test
    void validate_stark() {
        assertDoesNotThrow(() ->
                UserPayoutInformation.builder()
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .starknetAddress("0x00b112c41d5a1a2282ecbe1ca4f4eead5a6c19269e884fc23522ecb0581e3597")
                                .build())
                        .build().validateWalletAddresses());
    }

    @Test
    void validate_stark_invalid() {
        assertThrows(OnlyDustException.class, () ->
                UserPayoutInformation.builder()
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .starknetAddress("0xb112c41d5a1a2282ecbe1ca4f4eead5a6c19269e884fc23522ecb0581e3597")
                                .build())
                        .build().validateWalletAddresses());
    }

    @Test
    void validate_optimism() {
        assertDoesNotThrow(() ->
                UserPayoutInformation.builder()
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .optimismAddress("0x2C6277931328e2028C3DB10625D767de19151e92")
                                .build())
                        .build().validateWalletAddresses());
    }

    @Test
    void validate_optimism_invalid() {
        assertThrows(OnlyDustException.class, () ->
                UserPayoutInformation.builder()
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .optimismAddress("0x002C6277931328e2028C3DB10625D767de19151e92")
                                .build())
                        .build().validateWalletAddresses());
    }
}