package onlydust.com.marketplace.api.infrastructure.langchain.adapters;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import onlydust.com.marketplace.project.domain.port.output.LLMPort;

@AiService
public interface LangchainLLMAdapter extends LLMPort {
    @Override
    @SystemMessage("""
            You are a classifier assistant.
            You classify if a comment on a github issue is a demand for taking the issue.
            If the user state that he can work on it you have to include him as well.
            If the commenter suggest someone else you don't have to include him.
            Some messages only with an emoji hand or raised hand like 👋 can symbolize interest to take the issue, consider them TRUE.
            Be aware that some comments talk about other things than the issue.
            We only want to match contributors who want to take charge of the whole issue.
            Here are some examples of candidature comments:
               - "I want to take it"
               - "On it"
               - "Can I take this one?
            
            Example answers :
            
            User : Can I take this one?
            Assistant : TRUE
            
            User : I want to know more about this issue.
            Assistant : FALSE
            """)
    boolean isCommentShowingInterestToContribute(String comment);
}
