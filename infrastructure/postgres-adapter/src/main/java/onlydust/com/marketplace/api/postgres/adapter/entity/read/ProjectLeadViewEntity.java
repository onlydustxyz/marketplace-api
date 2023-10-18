package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Table(name = "project_leads")
@Data
@Entity
public class ProjectLeadViewEntity {

    @Id
    @Column(name = "project_id")
    UUID projectId;
    @Column(name = "user_id")
    UUID userId;

}
