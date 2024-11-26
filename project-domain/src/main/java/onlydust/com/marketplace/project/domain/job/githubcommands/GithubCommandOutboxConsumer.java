package onlydust.com.marketplace.project.domain.job.githubcommands;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.OutboxConsumer;
import onlydust.com.marketplace.project.domain.model.event.GithubCreateCommentCommand;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

@Slf4j
@AllArgsConstructor
public class GithubCommandOutboxConsumer implements OutboxConsumer {

    private final GithubCreateCommentCommandConsumer githubCreateCommentCommandConsumer;

    @Override
    public void process(Event event) {
        switch (event) {
            case GithubCreateCommentCommand c -> githubCreateCommentCommandConsumer.process(c);
            default -> throw internalServerError("Unexpected Github Command: " + event);
        }
    }

}
