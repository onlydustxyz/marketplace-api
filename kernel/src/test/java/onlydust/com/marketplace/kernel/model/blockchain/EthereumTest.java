package onlydust.com.marketplace.kernel.model.blockchain;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class EthereumTest {
    @ParameterizedTest()
    @ValueSource(strings = {
            "",
            "02937",
            "0x",
            "0x0asdf",
            "0x12345678901234567890123456789012345678901"
    })
    void should_reject_invalid_address(String value) {
        assertThatThrownBy(() -> Ethereum.accountAddress(value))
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
        assertDoesNotThrow(() -> Ethereum.accountAddress(value));
    }

    @Test
    void should_sanitize_value() {
        assertThat(Ethereum.accountAddress("0x1").toString()).isEqualTo("0x01");
        assertThat(Ethereum.accountAddress("0x01").toString()).isEqualTo("0x01");
        assertThat(Ethereum.accountAddress("0x123456789").toString()).isEqualTo("0x0123456789");
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
        assertThrows(OnlyDustException.class, () -> Ethereum.transactionHash(value));
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "0xe39906d57d0803f9af7d0d6e0b86c68e6662d26e4a8915c132d50d72869dcc0e",
            "0x1ad38e48099d649b36b6357364a175ed186418fb07a0fd8865b27dec28b24a53",
            "0xee7421645bda3d6ec073bf91a8ea9efe27a6aa62f640ec01e28411553f6199b7",
            "0x019cf81d9b6b0277869b550072365a86b3aa8f41438c084c3d767c215b54f9e2",
            "0x19cf81d9b6b0277869b550072365a86b3aa8f41438c084c3d767c215b54f9e2"
    })
    void should_accept_valid_hash(String value) {
        assertDoesNotThrow(() -> Ethereum.transactionHash(value));
    }

    @Test
    void should_generate_transaction_url() {
        assertThat(Ethereum.BLOCK_EXPLORER.url(Ethereum.transactionHash("0x1")).toString())
                .isEqualTo("https://etherscan.io/tx/0x01");
    }

    @Test
    void should_create_wallet_from_address() {
        assertThat(Ethereum.wallet("0x00000000219ab540356cBB839Cbe05303d7705Fa").asString())
                .isEqualTo("0x00000000219ab540356cBB839Cbe05303d7705Fa");

        assertThat(Ethereum.wallet("0x00000000219ab540356cBB839Cbe05303d7705Fa").accountAddress())
                .isEqualTo(Optional.of(Ethereum.accountAddress("0x00000000219ab540356cBB839Cbe05303d7705Fa")));

        assertThat(Ethereum.wallet("0x00000000219ab540356cBB839Cbe05303d7705Fa").ens())
                .isEmpty();
    }

    @Test
    void should_create_wallet_from_ens() {
        assertThat(Ethereum.wallet("vitalik.eth").asString())
                .isEqualTo("vitalik.eth");

        assertThat(Ethereum.wallet("vitalik.eth").accountAddress())
                .isEmpty();

        assertThat(Ethereum.wallet("vitalik.eth").ens())
                .isEqualTo(Optional.of(Ethereum.name("vitalik.eth")));
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "",
            "02937",
            "0x",
            "0x0asdf",
            "0x123456789012345678901234567890123456",
            "vitalik"
    })
    void should_reject_invalid_ens(String value) {
        assertThatThrownBy(() -> Ethereum.name(value))
                .isInstanceOf(OnlyDustException.class);
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "vitalik.eth",
            "abuisset87.stark.eth",
            "madame-michu.eth",
            "jp.morgan.eth"
    })
    void should_accept_valid_ens(String value) {
        assertDoesNotThrow(() -> Ethereum.name(value));
    }
}