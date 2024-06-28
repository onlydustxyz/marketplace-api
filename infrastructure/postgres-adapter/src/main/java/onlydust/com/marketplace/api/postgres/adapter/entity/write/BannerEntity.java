package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.project.domain.model.Banner;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "bannerId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BannerClosedByEntity> closedBy;

    public static BannerEntity of(Banner banner) {
        return BannerEntity.builder()
                .id(banner.id().value())
                .text(banner.text())
                .buttonText(banner.buttonText())
                .buttonIconSlug(banner.buttonIconSlug())
                .buttonLinkUrl(banner.buttonLinkUrl() == null ? null : banner.buttonLinkUrl().toString())
                .visible(banner.visible())
                .updatedAt(banner.updatedAt())
                .closedBy(banner.closedBy().stream().map(userId -> new BannerClosedByEntity(banner.id().value(), userId)).collect(toSet()))
                .build();
    }

    public Banner toDomain() {
        return new Banner(
                Banner.Id.of(id),
                text,
                buttonText,
                buttonIconSlug,
                buttonLinkUrl == null ? null : URI.create(buttonLinkUrl),
                visible,
                updatedAt,
                closedBy.stream().map(BannerClosedByEntity::userId).collect(toSet()));
    }
}
