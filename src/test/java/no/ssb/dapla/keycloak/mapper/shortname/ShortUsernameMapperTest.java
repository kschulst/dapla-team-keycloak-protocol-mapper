package no.ssb.dapla.keycloak.mapper.shortname;

import org.junit.jupiter.api.Test;

import static no.ssb.dapla.keycloak.mapper.shortname.ShortUsernameMapper.emailToShortUsername;

import static org.assertj.core.api.Assertions.assertThat;

class ShortUsernameMapperTest {

    @Test
    void testEmailToShortUsername() {
        assertThat(emailToShortUsername("john.doe@example.com")).isEqualTo("example-john-doe");
        assertThat(emailToShortUsername("john.doe@example")).isNull(); // Invalid email
        assertThat(emailToShortUsername(null)).isNull(); // Null email
        assertThat(emailToShortUsername("")).isNull(); // Empty email
        assertThat(emailToShortUsername("   ")).isNull(); // Blank email
    }

}