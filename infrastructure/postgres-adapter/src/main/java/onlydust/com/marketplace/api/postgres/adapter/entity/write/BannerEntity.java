package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.project.domain.model.Banner;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "banners", schema = "public")
@Accessors(fluent = true, chain = true)
public class BannerEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID id;

    @NonNull
    String text;
    String buttonText;
    String buttonIconSlug;
    String buttonLinkUrl;
    boolean visible;

    @NonNull
    ZonedDateTime updatedAt;

    public static BannerEntity of(Banner banner) {
        return BannerEntity.builder()
                .id(banner.id().value())
                .text(banner.text())
                .buttonText(banner.buttonText())
                .buttonIconSlug(banner.buttonIconSlug())
                .buttonLinkUrl(banner.buttonLinkUrl() == null ? null : banner.buttonLinkUrl().toString())
                .visible(banner.visible())
                .updatedAt(banner.updatedAt())
                .build();
    }
}
