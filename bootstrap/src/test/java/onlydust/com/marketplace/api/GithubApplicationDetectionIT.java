package onlydust.com.marketplace.api;

import onlydust.com.marketplace.api.infrastructure.langchain.adapters.LangchainLLMAdapter;
import onlydust.com.marketplace.api.it.api.AbstractMarketplaceApiIT;
import onlydust.com.marketplace.api.suites.tags.TagLLM;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TagLLM
public class GithubApplicationDetectionIT extends AbstractMarketplaceApiIT {
    @Autowired
    LangchainLLMAdapter langchainLLMAdapter;

    @DynamicPropertySource
    static void updateProperties(DynamicPropertyRegistry registry) {
        registry.add("langchain4j.open-ai.chat-model.base-url", () -> "https://api.openai.com/v1");
        registry.add("langchain4j.open-ai.chat-model.api-key", () -> "${OPENAI_API_KEY}");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "@rsodre I would like to work on this issue if it is not assigned to anyone",
            "Let me work on this. @clexmond",
            "Can I be assigned this issue please? I am ready to work?",
            "Can i do this?",
            "I have experience in smart contract development. Although this will be my first time contributing here I am confident in handling this task. I " +
            "estimate to deliver this within a day  if assigned.",
            "Hey @clexmond I can work on this issue! Let me know if it's still available!"
    })
    void should_detect_github_application(String comment) {
        assertTrue(langchainLLMAdapter.isCommentShowingInterestToContribute(comment));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Following is an overview of the [DiseL](https://github.com/DistributedComponents/disel) framework",
            "This would be very welcome! Please feel free to submit the PR.",
            "Sure, go",
            "@thomas192 still around and interested to do so? Let's try with 3 rust code if you feel confortable with it.",
            """
                    Hey @suiwater sorry for the too huge delay here.
                    That's a very good catch. Let's add the new param. If you're still around, happy to assign if you can tackle it.
                    """,
    })
    void should_not_detect_false_github_application(String comment) {
        assertFalse(langchainLLMAdapter.isCommentShowingInterestToContribute(comment));
    }
}
