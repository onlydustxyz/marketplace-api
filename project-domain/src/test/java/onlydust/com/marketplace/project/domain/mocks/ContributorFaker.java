package onlydust.com.marketplace.project.domain.mocks;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import onlydust.com.marketplace.project.domain.model.Contributor;

public class ContributorFaker {
    final Faker faker = new Faker();

    public Contributor contributor() {
        return Contributor.builder()
                .id(GithubUserIdentity.builder()
                        .githubUserId(faker.number().randomNumber() + faker.number().randomNumber())
                        .login(faker.name().username())
                        .avatarUrl(faker.internet().avatar())
                        .email(faker.internet().emailAddress())
                        .build())
                .isRegistered(faker.bool().bool())
                .build();
    }
}
