package onlydust.com.marketplace.api.webhook;

import lombok.Data;
import lombok.ToString;

import java.net.URI;

@ToString
@Data
public class Config {
    private URI url;
    private String environment;
    private String apiKey;
    private URI sendRejectedInvoiceMailUrl;
    private URI sendRewardsPaidMailUrl;
}