package no.ssb.dapla.keycloak.mapper.shortname;

import org.junit.jupiter.api.Test;

import static no.ssb.dapla.keycloak.mapper.shortname.ShortUsernameMapper.emailToShortUsername;

import static org.assertj.core.api.Assertions.assertThat;

class ShortUsernameMapperTest {

    @Test
    void testEmailToShortUsernameWithPrefix() {
        assertThat(emailToShortUsername("john.doe@example.com", true)).isEqualTo("example-john-doe");
        assertThat(emailToShortUsername("john.doe@example", true)).isNull(); // Invalid email
        assertThat(emailToShortUsername(null, true)).isNull(); // Null email
        assertThat(emailToShortUsername("", true)).isNull(); // Empty email
        assertThat(emailToShortUsername("   ", true)).isNull(); // Blank email
    }

    @Test
    void testEmailToShortUsernameWithoutPrefix() {
        assertThat(emailToShortUsername("john.doe@example.com", false)).isEqualTo("john-doe");
        assertThat(emailToShortUsername("john.doe@example", false)).isNull(); // Invalid email
        assertThat(emailToShortUsername(null, false)).isNull(); // Null email
        assertThat(emailToShortUsername("", false)).isNull(); // Empty email
        assertThat(emailToShortUsername("   ", false)).isNull(); // Blank email
    }

}