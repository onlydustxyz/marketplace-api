package onlydust.com.marketplace.api.webhook;

import java.net.URI;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Config {

  private URI url;
  private String environment;
}