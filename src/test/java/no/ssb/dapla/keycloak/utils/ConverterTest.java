package no.ssb.dapla.keycloak.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ConverterTest {

    @Test
    void shouldConvertStringToInteger() {
        Integer convertedValue = Converter.convert("123", Integer.class);
        assertThat(convertedValue).isEqualTo(123);
    }

    @Test
    void shouldConvertIntegerToString() {
        String convertedValue = Converter.convert(123, String.class);
        assertThat(convertedValue).isEqualTo("123");
    }

    @Test
    void shouldFailWhenConversionIsNotPossible() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> Converter.convert("123abc", Integer.class))
                .withMessageContaining("Cannot deserialize");
    }

    @Test
    void shouldReturnNullWhenFromValueIsNull() {
        String convertedValue = Converter.convert(null, String.class);
        assertThat(convertedValue).isNull();
    }

}
