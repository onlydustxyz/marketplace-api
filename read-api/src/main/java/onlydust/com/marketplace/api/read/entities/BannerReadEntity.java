package onlydust.com.marketplace.api.read.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.backoffice.api.contract.model.BannerPageItemResponse;
import onlydust.com.backoffice.api.contract.model.BannerResponse;
import org.hibernate.annotations.Immutable;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;


@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "banners", schema = "public")
@Immutable
@Accessors(fluent = true)
public class BannerReadEntity {
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

    public BannerPageItemResponse toBoPageItemResponse() {
        return new BannerPageItemResponse()
                .id(id)
                .text(text)
                .visible(visible);
    }

    public BannerResponse toBoResponse() {
        return new BannerResponse()
                .id(id)
                .text(text)
                .buttonText(buttonText)
                .buttonIconSlug(buttonIconSlug)
                .buttonLinkUrl(buttonLinkUrl == null ? null : URI.create(buttonLinkUrl))
                .visible(visible);
    }
}
