package onlydust.com.marketplace.api.bootstrap.it.bo;

import com.github.javafaker.Faker;
import lombok.NonNull;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeJuryVoteRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectLeadRepository;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.Committee.ProjectAnswer;
import onlydust.com.marketplace.project.domain.model.JuryCriteria;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.port.input.CommitteeFacadePort;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BackOfficeCommitteeAccountingApiIT extends AbstractMarketplaceBackOfficeApiIT {

    private UserAuthHelper.AuthenticatedBackofficeUser pierre;
    // Users
    private static final UUID anthoId = UUID.fromString("747e663f-4e68-4b42-965b-b5aebedcd4c4");
    private static final UUID olivierId = UUID.fromString("e461c019-ba23-4671-9b6c-3a5a18748af9");
    private static final UUID pacoId = UUID.fromString("f20e6812-8de8-432b-9c31-2920434fe7d0");

    // Projects
    private static final UUID apibaraId = UUID.fromString("2073b3b2-60f4-488c-8a0a-ab7121ed850c");
    private static final UUID bretzelId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");
    private static final UUID cairoFoundryId = UUID.fromString("8156fc5f-cec5-4f70-a0de-c368772edcd4");
    private static final UUID delugeId = UUID.fromString("ade75c25-b39f-4fdf-a03a-e2391c1bc371");
    private static final UUID starklingsId = UUID.fromString("6239cb20-eece-466a-80a0-742c1071dd3c");

    // Sponsors
    private static final UUID cocaColax = UUID.fromString("44c6807c-48d1-4987-a0a6-ac63f958bdae");

    private static final Faker faker = new Faker();
    private UUID committeeId;


    @Autowired
    CommitteeFacadePort committeeFacadePort;
    @Autowired
    CommitteeJuryVoteRepository committeeJuryVoteRepository;
    @Autowired
    ProjectLeadRepository projectLeadRepository;

    @BeforeEach
    void setUp() {
        pierre = userAuthHelper.authenticateBackofficeUser("pierre.oucif@gadz.org", List.of(BackofficeUser.Role.BO_READER,
                BackofficeUser.Role.BO_MARKETING_ADMIN));

        committeeId = fakeCommittee().id().value();
    }

    @Test
    void should_get_budget_allocations() {
    }

    private Committee fakeCommittee() {
        var committee = committeeFacadePort.createCommittee("My committee", ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(2));

        committee = committee.toBuilder()
                .sponsorId(cocaColax)
                .projectQuestions(IntStream.range(1, 10).mapToObj(i -> fakeProjectQuestion()).toList())
                .juryIds(List.of(anthoId, olivierId, pacoId))
                .juryCriteria(IntStream.range(1, 10).mapToObj(i1 -> fakeJuryCriteria()).toList())
                .votePerJury(2)
                .build();

        committeeFacadePort.update(committee);

        committeeFacadePort.updateStatus(committee.id(), Committee.Status.OPEN_TO_APPLICATIONS);
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, apibaraId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, bretzelId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, cairoFoundryId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, delugeId));
        committeeFacadePort.createUpdateApplicationForCommittee(committee.id(), fakeApplication(committee, starklingsId));

        committeeFacadePort.updateStatus(committee.id(), Committee.Status.OPEN_TO_VOTES);
        vote(committee, apibaraId, 1);
        vote(committee, bretzelId, 2);
        vote(committee, cairoFoundryId, 3);
        vote(committee, delugeId, 4);
        vote(committee, starklingsId, 5);

        committeeFacadePort.updateStatus(committee.id(), Committee.Status.CLOSED);

        return committee;
    }

    @NonNull
    private void vote(Committee committee, final UUID projectId, int score) {
        final var votes = committeeJuryVoteRepository.findAllByCommitteeIdAndProjectId(committee.id().value(), projectId);
        votes.forEach(v -> v.setScore(score));
        committeeJuryVoteRepository.saveAll(votes);
    }

    @NonNull
    private Committee.Application fakeApplication(Committee committee, UUID projectId) {
        final var projectLead = projectLeadRepository.findAll().stream().filter(pl -> pl.getProjectId().equals(projectId)).findFirst().orElseThrow();
        return new Committee.Application(projectLead.getUserId(),
                projectId,
                committee.projectQuestions().stream().map(q -> fakeProjectAnswer(q.id())).toList());
    }

    private ProjectQuestion fakeProjectQuestion() {
        return new ProjectQuestion(faker.lorem().paragraph(), faker.bool().bool());
    }

    private JuryCriteria fakeJuryCriteria() {
        return new JuryCriteria(faker.lorem().paragraph());
    }

    private ProjectAnswer fakeProjectAnswer(@NonNull ProjectQuestion.Id projectQuestionId) {
        return new ProjectAnswer(projectQuestionId, faker.lorem().paragraph());
    }
}
