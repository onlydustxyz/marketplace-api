package onlydust.com.marketplace.api.infrastructure.langchain.adapters;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import onlydust.com.marketplace.project.domain.port.output.LLMPort;

@AiService
public interface LangchainLLMAdapter extends LLMPort {
    @Override
    @UserMessage("""
            Based on the following issue comment taken from GitHub:
            "{{comment}}"
            Does it express an interest from the author in helping to resolve the issue?
            """)
    boolean isCommentShowingInterestToContribute(@V("comment") String comment);
}
