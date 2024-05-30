package onlydust.com.marketplace.bff.read.entities.project;

import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
public class LanguageLinkJson {
    @NonNull
    UUID id;
    @NonNull
    String name;
    String logoUrl;
    String bannerUrl;
}
