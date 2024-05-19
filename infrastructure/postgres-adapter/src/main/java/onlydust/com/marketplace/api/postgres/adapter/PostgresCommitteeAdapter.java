package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.api.postgres.adapter.entity.enums.CommitteeStatusEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.backoffice.BoCommitteeQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeProjectAnswerEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.CommitteeProjectQuestionEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeApplicationRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeProjectAnswerViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeProjectQuestionRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.CommitteeRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.backoffice.BoCommitteeQueryRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;
import onlydust.com.marketplace.project.domain.port.output.CommitteeStoragePort;
import onlydust.com.marketplace.project.domain.view.CommitteeLinkView;
import onlydust.com.marketplace.project.domain.view.CommitteeView;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class PostgresCommitteeAdapter implements CommitteeStoragePort {

    private final CommitteeRepository committeeRepository;
    private final BoCommitteeQueryRepository boCommitteeQueryRepository;
    private final CommitteeApplicationRepository committeeApplicationRepository;
    private final CommitteeProjectQuestionRepository committeeProjectQuestionRepository;
    private final CommitteeProjectAnswerViewRepository committeeProjectAnswerViewRepository;

    @Override
    @Transactional
    public Committee save(Committee committee) {
        return committeeRepository.save(CommitteeEntity.fromDomain(committee)).toDomain();
    }

    @Override
    @Transactional
    public void saveProjectQuestions(Committee.Id committeeId, List<ProjectQuestion> projectQuestions) {
        committeeProjectQuestionRepository.saveAll(projectQuestions.stream().map(projectQuestion -> CommitteeProjectQuestionEntity.fromDomain(committeeId,
                projectQuestion)).toList());
    }

    @Override
    @Transactional
    public void deleteAllProjectQuestions(Committee.@NonNull Id committeeId) {
        committeeProjectQuestionRepository.deleteAllByCommitteeId(committeeId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommitteeLinkView> findAll(Integer pageIndex, Integer pageSize) {
        final var committees = committeeRepository.findAll(PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "techCreatedAt")));
        return Page.<CommitteeLinkView>builder()
                .content(committees.getContent().stream().map(CommitteeEntity::toLink).toList())
                .totalPageNumber(committees.getTotalPages())
                .totalItemNumber((int) committees.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommitteeView> findById(Committee.Id committeeId) {
        return boCommitteeQueryRepository.findById(committeeId.value()).map(BoCommitteeQueryEntity::toView);
    }

    @Override
    @Transactional
    public void updateStatus(Committee.Id committeeId, Committee.Status status) {
        committeeRepository.updateStatus(committeeId.value(), CommitteeStatusEntity.fromDomain(status).name());
    }

    @Override
    @Transactional
    public void saveApplication(Committee.Id committeeId, Committee.Application application) {
        committeeApplicationRepository.saveAll(CommitteeProjectAnswerEntity.fromDomain(committeeId, application));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectAnswerView> getApplicationAnswers(Committee.Id committeeId, UUID projectId) {
        return committeeProjectAnswerViewRepository.findByCommitteeIdAndAndProjectId(committeeId.value(), projectId).stream()
                .map(committeeProjectAnswerView -> new ProjectAnswerView(ProjectQuestion.Id.of(committeeProjectAnswerView.getQuestionId()),
                        committeeProjectAnswerView.getProjectQuestion().getQuestion(), committeeProjectAnswerView.getProjectQuestion().getRequired(),
                        committeeProjectAnswerView.getAnswer()))
                .toList();
    }
}
