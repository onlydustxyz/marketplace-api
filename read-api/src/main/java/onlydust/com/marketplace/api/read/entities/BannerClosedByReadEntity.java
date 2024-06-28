package onlydust.com.marketplace.api.read.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Table(name = "banners_closed_by", schema = "public")
@Immutable
@Accessors(fluent = true)
@IdClass(BannerClosedByReadEntity.PrimaryKey.class)
public class BannerClosedByReadEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID bannerId;

    @Id
    @EqualsAndHashCode.Include
    @NonNull
    UUID userId;

    @EqualsAndHashCode
    public static class PrimaryKey {
        UUID bannerId;
        UUID userId;
    }
}
