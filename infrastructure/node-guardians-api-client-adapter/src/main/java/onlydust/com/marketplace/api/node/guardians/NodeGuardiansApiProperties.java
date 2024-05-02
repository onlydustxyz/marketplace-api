package onlydust.com.marketplace.api.node.guardians;

import lombok.Data;

import java.net.URI;

@Data
public class NodeGuardiansApiProperties {
    URI baseUri;
    String apiKey;
}
