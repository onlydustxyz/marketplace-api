package onlydust.com.marketplace.api.domain.model.blockchain.evm.ethereum;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Name {

  private static final Pattern ENS_PATTERN = Pattern.compile("^.+\\.eth$");
  private final String ens;

  public Name(final String ens) {
    if (!ENS_PATTERN.matcher(ens).matches()) {
      throw badRequest("Provided ENS is not valid");
    }

    this.ens = ens;
  }

  public String asString() {
    return ens;
  }
}
