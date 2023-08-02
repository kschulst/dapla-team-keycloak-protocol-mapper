package no.ssb.dapla.keycloak.mappers.shortname;

import com.google.auto.service.AutoService;
import no.ssb.dapla.keycloak.mappers.AbstractTokenMapper;
import no.ssb.dapla.keycloak.mappers.ConfigPropertyType;
import no.ssb.dapla.keycloak.mappers.CouldNotDeduceClaimException;
import no.ssb.dapla.keycloak.utils.Email;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.representations.IDToken;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(ProtocolMapper.class)
public class ShortUsernameMapper2 extends AbstractTokenMapper {

    public static final String PROVIDER_ID = "oidc-dapla-short-username-mapper2";

    public static class ConfigPropertyKey {
        public static final String USE_DOMAIN_AS_PREFIX = "dapla.short-username.use-domain-as-prefix";
        public static final String DOMAINS_NOT_USED_AS_PREFIX = "dapla.short-username.domains-not-used-as-prefix";
    }

    public ShortUsernameMapper2() {
        super(PROVIDER_ID,

                configProperty()
                        .name(ConfigPropertyKey.USE_DOMAIN_AS_PREFIX)
                        .label("Use domain as prefix?")
                        .helpText("""
                                Enable this to include the domain part of the email as a prefix to the shortname.
                                You can use this in combination with a comma-separated list of domain names that should
                                be excluded from being used as prefixes.""")
                        .type(ConfigPropertyType.BOOLEAN)
                        .defaultValue(Boolean.TRUE)
                        .build(),

                configProperty()
                        .name(ConfigPropertyKey.DOMAINS_NOT_USED_AS_PREFIX)
                        .label("Domains not used as prefix")
                        .helpText("""
                                A comma-separated list of domains that will not be used as prefixes to the shortname.
                                This is only relevant if 'Use domain as prefix' (see above) is true.
                                Leave this empty to have all usernames be prefixed with domain.
                                
                                Example: Specify your primary domain name (such as 'domain.com') so that only users
                                from other domains will receive a prefix to their short_username claim
                                john.doe@domain.com -> john-doe
                                jane.doe@anotherdomain.com -> anotherdomain-jane-doe""")
                        .type(ConfigPropertyType.MULTIVALUED_STRING)
                        .build()
        );
    }

    @Override
    protected String helpText() {
        return """
                Transform a user's email to a short username claim (RFC-1123 compliant string).
                Example: john.doe@example.com -> example-john-doe""";
    }

    @Override
    protected Object mapToClaim(final IDToken token,
                                final ProtocolMapperModel mappingModel,
                                final UserSessionModel userSession,
                                final KeycloakSession keycloakSession,
                                final ClientSessionContext clientSessionCtx) {
        String email = userSession.getUser().getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new CouldNotDeduceClaimException("Email was null or empty. Unable to deduce shortname.");
        }

        boolean useDomainAsPrefix = useDomainAsPrefix(mappingModel);
        Set<String> domainsNotUsedAsPrefix = domainsNotUsedAsPrefix(mappingModel);
        return emailToShortUsername(email, useDomainAsPrefix, domainsNotUsedAsPrefix);
    }

    static String emailToShortUsername(String email, boolean useDomainAsPrefix, Set<String> domainsNotUsedAsPrefix) {
        String localPart = Email.localPart(email)
                .orElseThrow(() -> new CouldNotDeduceClaimException("Unable to retrieve local part from email " + email));

        String domainPart = null;
        if (useDomainAsPrefix) {
            domainPart = Email.domainPartWithoutTld(email)
                    .filter(d -> !domainsNotUsedAsPrefix.contains(d))
                    .orElse(null);
        }

        return asRfc1123(domainPart != null
                ? domainPart + "-" + localPart
                : localPart
        );
    }

    boolean useDomainAsPrefix(final ProtocolMapperModel mappingModel) {
        return getConfigBoolean(mappingModel, ConfigPropertyKey.USE_DOMAIN_AS_PREFIX);
    }

    Set<String> domainsNotUsedAsPrefix(final ProtocolMapperModel mappingModel) {
        String config = getConfigString(mappingModel, ConfigPropertyKey.DOMAINS_NOT_USED_AS_PREFIX);

        return (config == null || config.isBlank())
                ? Set.of()
                : Arrays.stream(config.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private static String asRfc1123(String s) {
        return s.replaceAll("[^A-Za-z0-9]", "-").toLowerCase();
    }

}
