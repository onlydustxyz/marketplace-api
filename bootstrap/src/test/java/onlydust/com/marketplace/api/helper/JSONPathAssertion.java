package onlydust.com.marketplace.api.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONArray;
import org.intellij.lang.annotations.Language;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.function.Consumer;

public class JSONPathAssertion {

    public static Consumer<Object> jsonObjectEquals(@Language("JSON") String expectedJson) {
        return jsonObjectEquals(expectedJson, false);
    }

    public static Consumer<Object> jsonObjectEquals(@Language("JSON") String expectedJson, boolean strict) {
        final var objectMapper = new ObjectMapper();

        return new Consumer<Object>() {
            @Override
            public void accept(Object json) {
                try {
                    if (json instanceof JSONArray jsonArray) {
                        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(jsonArray.get(0)), strict);
                    } else {
                        JSONAssert.assertEquals(expectedJson, objectMapper.writeValueAsString(json), strict);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
