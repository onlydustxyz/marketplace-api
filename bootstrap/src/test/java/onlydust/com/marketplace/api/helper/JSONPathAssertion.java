package onlydust.com.marketplace.api.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import org.intellij.lang.annotations.Language;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.function.Consumer;

public class JSONPathAssertion {

    public static Consumer<Object> jsonObjectEquals(@Language("JSON") String expectedJson) {
        final var objectMapper = new ObjectMapper();

        return new Consumer<Object>() {
            @Override
            public void accept(Object json) {
                try {
                    if (json instanceof JSONArray jsonArray) {
                        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(jsonArray.get(0)), true);
                    } else {
                        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(json), true);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
