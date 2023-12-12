package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.Map;

@EqualsAndHashCode
@Data
@Entity
public class GithubRepoViewEntity {
    @Id
    Long id;
    String owner;
    String name;
    String htmlUrl;
    Date updatedAt;
    String Description;
    Long starsCount;
    Long forksCount;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    Map<String, Long> languages;
}
