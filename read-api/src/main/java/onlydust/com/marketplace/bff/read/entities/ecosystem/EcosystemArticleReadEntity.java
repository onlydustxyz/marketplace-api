package onlydust.com.marketplace.bff.read.entities.ecosystem;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.EcosystemArticle;
import org.hibernate.annotations.Immutable;

import java.net.URI;
import java.util.UUID;

@Entity
@NoArgsConstructor(force = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Value
@Table(name = "ecosystem_articles", schema = "public")
@Immutable
@Accessors(fluent = true)
public class EcosystemArticleReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    private UUID id;

    private @NonNull String title;
    private @NonNull String description;
    private @NonNull String url;
    private @NonNull String image_url;

    public EcosystemArticle toDto() {
        return new EcosystemArticle()
                .title(title)
                .description(description)
                .url(URI.create(url))
                .imageUrl(URI.create(image_url))
                ;
    }
}
