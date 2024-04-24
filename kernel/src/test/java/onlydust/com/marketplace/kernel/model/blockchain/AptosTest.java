package onlydust.com.marketplace.kernel.model.blockchain;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AptosTest {
    @ParameterizedTest()
    @ValueSource(strings = {
            "",
            "02937",
            "0x",
            "0x0asdf",
            "0x12345678901234567890123456789012345678901234567890123456789012345"
    })
    void should_reject_invalid_address(String value) {
        assertThatThrownBy(() -> Aptos.accountAddress(value))
                .isInstanceOf(OnlyDustException.class);
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "0x00000000219ab540356cBB839Cbe05303d7705Fa",
            "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
            "0xBE0eB53F46cd790Cd13851d5EFf43D12404d33E8",
            "0xDA9dfA130Df4dE4673b89022EE50ff26f6EA73Cf",
            "0x40B38765696e3d5d8d9d834D8AaD4bB6e418E489"
    })
    void should_accept_valid_address(String value) {
        assertDoesNotThrow(() -> Aptos.accountAddress(value));
    }

    @Test
    void should_sanitize_value() {
        assertThat(Aptos.accountAddress("0x1").toString()).isEqualTo("0x01");
        assertThat(Aptos.accountAddress("0x01").toString()).isEqualTo("0x01");
        assertThat(Aptos.accountAddress("0x123456789").toString()).isEqualTo("0x0123456789");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "",
            "02937",
            "0x",
            "0x0asdf",
            "0x01234567890123456789012345678901234567890123456789012345678901234"
    })
    void should_reject_invalid_hash(String value) {
        assertThrows(OnlyDustException.class, () -> Aptos.transactionHash(value));
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "0x655cc112b1beaee11d5da8ea53cb9fe9ac315827e0ffed5ae746176a585d4fb4",
            "0x024b05cdec5467c2ea102ceedda219f6c75f3710317ba51d3864873381607407",
            "0xccc370c2edd90ec64574a1cbc631a194f3cfd5dd6dfce9432860a497a025e090",
            "0x4906e5e67af366789ce5568c6573e256a82863b9e901bb3b51d07674bacfeb23"
    })
    void should_accept_valid_hash(String value) {
        assertDoesNotThrow(() -> Aptos.transactionHash(value));
    }

    @Test
    void should_generate_transaction_url() {
        assertThat(Aptos.BLOCK_EXPLORER.url(Aptos.transactionHash(
                "0x4906e5e67af366789ce5568c6573e256a82863b9e901bb3b51d07674bacfeb23")).toString())
                .isEqualTo("https://aptoscan.com/transaction/0x4906e5e67af366789ce5568c6573e256a82863b9e901bb3b51d07674bacfeb23");
    }
}