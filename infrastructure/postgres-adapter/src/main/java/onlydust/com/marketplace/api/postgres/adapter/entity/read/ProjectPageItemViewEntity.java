package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.ProjectVisibilityEnumEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "project_details", schema = "public")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@TypeDef(name = "project_visibility", typeClass = PostgreSQLEnumType.class)
public class ProjectPageItemViewEntity {
    @Id
    @Column(name = "project_id")
    UUID projectId;
    Boolean hiring;
    @Column(name = "logo_url")
    String logoUrl;
    String key;
    String name;
    @Column(name = "short_description")
    String shortDescription;
    @Column(name = "long_description")
    String longDescription;
    @Type(type = "project_visibility")
    @Enumerated(EnumType.STRING)
    ProjectVisibilityEnumEntity visibility;
    Integer rank;
    Integer repoCount;
    Integer contributorsCount;
    Integer projectLeadCount;
    Boolean isPendingProjectLead;
    @Type(type = "jsonb")
    List<Sponsor> sponsors;
    @Type(type = "jsonb")
    List<ProjectLead> projectLeads;
    @Type(type = "jsonb")
    List<Map<String, Long>> technologies;

    @EqualsAndHashCode
    public static class ProjectLead {
        @JsonProperty("id")
        UUID id;
        @JsonProperty("url")
        String url;
        @JsonProperty("avatarUrl")
        String avatarUrl;
        @JsonProperty("login")
        String login;
        @JsonProperty("githubId")
        Long githubId;
    }


    @EqualsAndHashCode
    public static class Sponsor {
        @JsonProperty("url")
        String url;
        @JsonProperty("logoUrl")
        String logoUrl;
        @JsonProperty("id")
        UUID id;
    }

}
