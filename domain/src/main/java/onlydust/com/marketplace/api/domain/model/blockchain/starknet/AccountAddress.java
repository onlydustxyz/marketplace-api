package onlydust.com.marketplace.api.domain.model.blockchain.starknet;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import onlydust.com.marketplace.api.domain.model.blockchain.Hash;

@EqualsAndHashCode(callSuper = true)
public class AccountAddress extends Hash {

  private static final int MAX_BYTE_COUNT = 32;

  public AccountAddress(final @NonNull String address) {
    super(MAX_BYTE_COUNT, address);
  }
}
