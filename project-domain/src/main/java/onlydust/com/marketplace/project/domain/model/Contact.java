package onlydust.com.marketplace.project.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@AllArgsConstructor
public class Contact {
    Channel channel;
    String contact;
    Visibility visibility;

    @AllArgsConstructor
    @Getter
    public enum Channel {
        EMAIL,
        TELEGRAM,
        TWITTER,
        DISCORD,
        LINKEDIN,
        WHATSAPP
    }

    @AllArgsConstructor
    @Getter
    public enum Visibility {
        PRIVATE, PUBLIC
    }

}
