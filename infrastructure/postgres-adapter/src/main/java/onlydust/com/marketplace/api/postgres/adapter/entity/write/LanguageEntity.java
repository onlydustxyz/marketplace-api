package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.project.domain.model.Language;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Entity
@Table(name = "languages", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Accessors(fluent = true)
@ToString
public class LanguageEntity {
    @Id
    @EqualsAndHashCode.Include
    private @NonNull UUID id;
    private @NonNull String name;
    private @NonNull String slug;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "languageId")
    private List<LanguageFileExtensionEntity> fileExtensions;
    private String logoUrl;
    private String bannerUrl;

    public static LanguageEntity of(Language language) {
        return new LanguageEntity(language.id().value(),
                language.name(),
                language.slug(),
                language.fileExtensions().stream().map(e -> new LanguageFileExtensionEntity(e, language.id().value())).toList(),
                isNull(language.logoUrl()) ? null : language.logoUrl().toString(),
                isNull(language.bannerUrl()) ? null : language.bannerUrl().toString());
    }

    public Language toDomain() {
        return new Language(Language.Id.of(id),
                name,
                slug,
                isNull(fileExtensions) ? Set.of() : fileExtensions.stream().map(LanguageFileExtensionEntity::extension).collect(Collectors.toSet()),
                isNull(logoUrl) ? null : URI.create(logoUrl),
                isNull(bannerUrl) ? null : URI.create(bannerUrl));
    }
}
