package onlydust.com.marketplace.api.infrastructure.langchain.adapters;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;

import static org.assertj.core.api.Assertions.assertThat;

class LangchainLLMAdapterTest {
    //    @Test
    void isCommentShowingInterestToContribute() {
        final var adapter = AiServices.create(LangchainLLMAdapter.class, OpenAiChatModel.withApiKey("demo"));

        assertThat(adapter.isCommentShowingInterestToContribute("I would like to contribute to this issue")).isTrue();
        assertThat(adapter.isCommentShowingInterestToContribute("This issue would be very useful for my project")).isFalse();
    }
}
