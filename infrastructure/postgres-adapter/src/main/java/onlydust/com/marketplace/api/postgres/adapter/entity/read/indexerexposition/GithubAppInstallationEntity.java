package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexerexposition;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Data
@EqualsAndHashCode
@Entity
@Table(schema = "indexer_exp", name = "github_app_installations")
@Immutable
public class GithubAppInstallationEntity {
    @Id
    Long id;
    @OneToOne
    GithubAccountEntity account;
}
