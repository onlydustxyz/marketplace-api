package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "banners_closed_by", schema = "public")
@Accessors(fluent = true, chain = true)
@IdClass(BannerClosedByEntity.PrimaryKey.class)
public class BannerClosedByEntity {
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
