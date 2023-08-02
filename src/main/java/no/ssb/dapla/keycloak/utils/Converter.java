package no.ssb.dapla.keycloak.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Converter {

    private static final ObjectMapper OBJECT_MAPER = new ObjectMapper();

    public static <T> T convert(Object fromValue, Class<T> toValueType) {
        return OBJECT_MAPER.convertValue(fromValue, toValueType);
    }

}
