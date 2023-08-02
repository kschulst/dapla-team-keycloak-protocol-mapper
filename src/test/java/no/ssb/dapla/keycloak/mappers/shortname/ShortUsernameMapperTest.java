package no.ssb.dapla.keycloak.mappers.shortname;
import no.ssb.dapla.keycloak.mappers.CouldNotDeduceClaimException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.ProtocolMapperModel;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShortUsernameMapperTest {

    @Mock
    ProtocolMapperModel protocolMapperModel;

    ShortUsernameMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ShortUsernameMapper();
    }

    @Test
    void testEmailToShortUsername() throws Exception {
        String email = "test.user@domain.com";
        boolean useDomainAsPrefix = true;
        Set<String> domainsNotUsedAsPrefix = new HashSet<>();
        domainsNotUsedAsPrefix.add("anotherdomain.com");

        String result = mapper.emailToShortUsername(email, useDomainAsPrefix, domainsNotUsedAsPrefix);
        assertThat(result).isEqualTo("domain-test-user");
    }

    @Test
    void testEmailToShortUsername_NoPrefix() throws Exception {
        String email = "test.user@domain.com";
        boolean useDomainAsPrefix = false;
        Set<String> domainsNotUsedAsPrefix = new HashSet<>();

        String result = mapper.emailToShortUsername(email, useDomainAsPrefix, domainsNotUsedAsPrefix);
        assertThat(result).isEqualTo("test-user");
    }

    @Test
    void testEmailToShortUsername_NullEmail() {
        String email = null;
        boolean useDomainAsPrefix = true;
        Set<String> domainsNotUsedAsPrefix = new HashSet<>();

        assertThatThrownBy(() -> mapper.emailToShortUsername(email, useDomainAsPrefix, domainsNotUsedAsPrefix))
                .isInstanceOf(CouldNotDeduceClaimException.class)
                .hasMessage("Unable to retrieve local part from email null");
    }

    @Test
    void testEmailToShortUsername_EmptyEmail() {
        String email = "";
        boolean useDomainAsPrefix = true;
        Set<String> domainsNotUsedAsPrefix = new HashSet<>();

        assertThatThrownBy(() -> mapper.emailToShortUsername(email, useDomainAsPrefix, domainsNotUsedAsPrefix))
                .isInstanceOf(CouldNotDeduceClaimException.class)
                .hasMessage("Unable to retrieve local part from email ");
    }

    @Test
    void testEmailToShortUsername_BlankEmail() {
        String email = " ";
        boolean useDomainAsPrefix = true;
        Set<String> domainsNotUsedAsPrefix = new HashSet<>();

        assertThatThrownBy(() -> mapper.emailToShortUsername(email, useDomainAsPrefix, domainsNotUsedAsPrefix))
                .isInstanceOf(CouldNotDeduceClaimException.class)
                .hasMessage("Unable to retrieve local part from email  ");
    }

    @Test
    void testUseDomainAsPrefix() {
        Map<String, String> config = new HashMap<>();
        config.put(ShortUsernameMapper.ConfigPropertyKey.USE_DOMAIN_AS_PREFIX, "true");
        when(protocolMapperModel.getConfig()).thenReturn(config);

        boolean result = mapper.useDomainAsPrefix(protocolMapperModel);
        assertThat(result).isTrue();
    }

    @Test
    void testDomainsNotUsedAsPrefix() {
        Map<String, String> config = new HashMap<>();
        config.put(ShortUsernameMapper.ConfigPropertyKey.DOMAINS_NOT_USED_AS_PREFIX, "domain1.com, domain2.com");
        when(protocolMapperModel.getConfig()).thenReturn(config);

        Set<String> result = mapper.domainsNotUsedAsPrefix(protocolMapperModel);
        assertThat(result).contains("domain1.com", "domain2.com");
    }

    @Test
    void testDomainsNotUsedAsPrefix_NullConfig() {
        Map<String, String> config = new HashMap<>();
        config.put(ShortUsernameMapper.ConfigPropertyKey.DOMAINS_NOT_USED_AS_PREFIX, null);
        when(protocolMapperModel.getConfig()).thenReturn(config);

        Set<String> result = mapper.domainsNotUsedAsPrefix(protocolMapperModel);
        assertThat(result).isEmpty();
    }

    @Test
    void testDomainsNotUsedAsPrefix_EmptyConfig() {
        Map<String, String> config = new HashMap<>();
        config.put(ShortUsernameMapper.ConfigPropertyKey.DOMAINS_NOT_USED_AS_PREFIX, "");
        when(protocolMapperModel.getConfig()).thenReturn(config);

        Set<String> result = mapper.domainsNotUsedAsPrefix(protocolMapperModel);
        assertThat(result).isEmpty();
    }

    @Test
    void testDomainsNotUsedAsPrefix_BlankConfig() {
        Map<String, String> config = new HashMap<>();
        config.put(ShortUsernameMapper.ConfigPropertyKey.DOMAINS_NOT_USED_AS_PREFIX, " ");
        when(protocolMapperModel.getConfig()).thenReturn(config);

        Set<String> result = mapper.domainsNotUsedAsPrefix(protocolMapperModel);
        assertThat(result).isEmpty();
    }

}
