package onlydust.com.marketplace.project.domain.view;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.ProjectQuestion;

public record ProjectAnswerView(@NonNull ProjectQuestion.Id questionId, @NonNull String question, @NonNull Boolean required, String answer) {
}
