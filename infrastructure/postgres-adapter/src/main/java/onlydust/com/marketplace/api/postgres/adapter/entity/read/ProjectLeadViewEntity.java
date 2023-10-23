package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
public class ProjectLeadViewEntity {

    @EmbeddedId
    public ProjectLeadIdEntity id;

    @Embeddable
    @Data
    public static class ProjectLeadIdEntity implements Serializable {
        @Column(name = "project_id")
        UUID projectId;
        @Column(name = "user_id")
        UUID userId;
    }

}
