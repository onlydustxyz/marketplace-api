package onlydust.com.marketplace.project.domain.mocks;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.project.domain.model.Contributor;
import onlydust.com.marketplace.project.domain.model.GithubUserIdentity;

public class ContributorFaker {
    final Faker faker = new Faker();

    public Contributor contributor() {
        return Contributor.builder()
                .id(GithubUserIdentity.builder()
                        .githubUserId(faker.number().randomNumber() + faker.number().randomNumber())
                        .githubLogin(faker.name().username())
                        .githubAvatarUrl(faker.internet().avatar())
                        .build())
                .isRegistered(faker.bool().bool())
                .build();
    }
}
