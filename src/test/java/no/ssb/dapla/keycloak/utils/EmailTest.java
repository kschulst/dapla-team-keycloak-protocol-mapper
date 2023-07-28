package no.ssb.dapla.keycloak.utils;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailTest {

    @Test
    void localPart() {
        assertThat(Email.localPart("john.doe@example.com")).isEqualTo(Optional.of("john_doe"));
        assertThat(Email.localPart("john.doe@example")).isEqualTo(Optional.empty()); // Invalid email
        assertThat(Email.localPart(null)).isEqualTo(Optional.empty()); // Null email
        assertThat(Email.localPart("")).isEqualTo(Optional.empty()); // Empty email
        assertThat(Email.localPart("   ")).isEqualTo(Optional.empty()); // Blank email
    }

    @Test
    void domainPart() {
        assertThat(Email.domainPart("john.doe@example.com")).isEqualTo(Optional.of("example.com"));
        assertThat(Email.domainPart("john.doe@example")).isEqualTo(Optional.empty()); // Invalid email
        assertThat(Email.domainPart(null)).isEqualTo(Optional.empty()); // Null email
        assertThat(Email.domainPart("")).isEqualTo(Optional.empty()); // Empty email
        assertThat(Email.domainPart("   ")).isEqualTo(Optional.empty()); // Blank email
    }

    @Test
    void domainPartWithoutTld() {
        assertThat(Email.domainPartWithoutTld("john.doe@example.com")).isEqualTo(Optional.of("example"));
        assertThat(Email.domainPartWithoutTld("john.doe@example")).isEqualTo(Optional.empty()); // Invalid email
        assertThat(Email.domainPartWithoutTld(null)).isEqualTo(Optional.empty()); // Null email
        assertThat(Email.domainPartWithoutTld("")).isEqualTo(Optional.empty()); // Empty email
        assertThat(Email.domainPartWithoutTld("   ")).isEqualTo(Optional.empty()); // Blank email
    }

}