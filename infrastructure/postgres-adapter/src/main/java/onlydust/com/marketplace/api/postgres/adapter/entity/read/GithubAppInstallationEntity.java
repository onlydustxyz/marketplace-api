package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@EqualsAndHashCode
@Entity
@Table(schema = "indexer_exp", name = "github_app_installations")
public class GithubAppInstallationEntity {
    @Id
    Long id;
    @OneToOne
    GithubAccountEntity account;
}
