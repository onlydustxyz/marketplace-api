package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "language_file_extensions", schema = "public")
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Getter
@Accessors(fluent = true)
@ToString
public class LanguageFileExtensionEntity {
    @Id
    @NonNull
    private String extension;
    @NonNull
    @Column(name = "language_id")
    private UUID languageId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "language_id", insertable = false, updatable = false)
    private LanguageEntity language;

}
