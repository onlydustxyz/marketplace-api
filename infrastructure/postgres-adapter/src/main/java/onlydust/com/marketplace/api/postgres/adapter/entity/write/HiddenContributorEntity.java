package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "hidden_contributors", schema = "public")
public class HiddenContributorEntity {
    @EmbeddedId
    Id id;

    @Builder
    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        UUID projectId;
        UUID projectLeadId;
        Long contributorGithubUserId;
    }
}