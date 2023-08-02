package no.ssb.dapla.keycloak.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonTest {

    @Test
    void testToObjectWithTypeReference() {
        String json = "{\"key\":\"value\"}";
        TypeReference<HashMap<String, String>> type = new TypeReference<HashMap<String, String>>() {};
        Map<String, String> result = Json.toObject(type, json);

        assertThat(result)
                .isNotNull()
                .containsEntry("key", "value");
    }

    @Test
    void testToObjectWithTypeReferenceIOException() {
        String json = "not a json";
        TypeReference<HashMap<String, String>> type = new TypeReference<HashMap<String, String>>() {};

        assertThatThrownBy(() -> Json.toObject(type, json))
                .isInstanceOf(Json.JsonException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void testFrom() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        String json = Json.from(map);

        assertThat(json)
                .isNotNull()
                .isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    void testPrettyFromWithObject() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");

        String prettyJson = Json.prettyFrom(map);

        assertThat(prettyJson)
                .isNotNull()
                .isEqualTo("{\n  \"key\" : \"value\"\n}");
    }

    @Test
    void testPrettyFromWithString() {
        String json = "{\"key\":\"value\"}";

        String prettyJson = Json.prettyFrom(json);

        assertThat(prettyJson)
                .isNotNull()
                .isEqualTo("{\n  \"key\" : \"value\"\n}");
    }

}
