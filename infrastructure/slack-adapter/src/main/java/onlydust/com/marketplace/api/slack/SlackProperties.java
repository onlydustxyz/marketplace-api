package onlydust.com.marketplace.api.slack;

import lombok.Data;

@Data
public class SlackProperties {
    String token;
    String environment;
    String kycKybChannel;
    String devRelChannel;
    Boolean tagAllChannel;
}
