package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LanguageView {
    UUID id;
    String name;
    String logoUrl;
    String bannerUrl;
}
