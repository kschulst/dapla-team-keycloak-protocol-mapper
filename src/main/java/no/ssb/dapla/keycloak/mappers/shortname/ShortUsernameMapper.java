package no.ssb.dapla.keycloak.mappers.shortname;

import com.google.auto.service.AutoService;
import no.ssb.dapla.keycloak.BuildInfo;
import no.ssb.dapla.keycloak.utils.Email;
import no.ssb.dapla.keycloak.utils.Json;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: check warning message: oidc-dapla-short-username-mapper (no.ssb.dapla.keycloak.mapper.shortname.ShortUsernameMapper) is implementing the internal SPI protocol-mapper. This SPI is internal and may change without notice

/*
 * ShortnameMapper is a Keycloak protocol mapper that looks up a user's associated email and transforms it to
 * a [RFC-1123](https://datatracker.ietf.org/doc/html/rfc1123) compliant name.
 */
@AutoService(ProtocolMapper.class)
public class ShortUsernameMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static class ConfigKey {
        public static final String VERBOSE_LOGGING = "short-username.verbose-logging";
        public static final String USE_DOMAIN_AS_PREFIX = "short-username.use-domain-as-prefix";
        public static final String DOMAINS_NOT_USED_AS_PREFIX = "short-username.domains-not-used-as-prefix";
    }

    private static final Logger log = Logger.getLogger(ShortUsernameMapper.class);

    /*
     * A config which keycloak uses to display a generic dialog to configure the token.
     */
    static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    /*
     * The ID of the token mapper. Is public, because we need this id in our data-setup project to
     * configure the protocol mapper in keycloak.
     */
    public static final String PROVIDER_ID = "oidc-dapla-short-username-mapper";

    static {
        ProviderConfigProperty property;

        // Let the user define under which claim name (key) the protocol mapper writes its value.
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);

        // Let the user define for which tokens the protocol mapper is executed (access token,
        // id token, user info).
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, ShortUsernameMapper.class);

        // Let the user specify if logging should be verbose
        property = new ProviderConfigProperty();
        property.setName(ShortUsernameMapper.ConfigKey.VERBOSE_LOGGING);
        property.setLabel("Verbose logging");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setHelpText("Enable this to include extra verbose logging in the Keycloak application logs. " +
                "This can be used for pinpointing problems without having to adjust the Keycloak logging config.");
        property.setDefaultValue(Boolean.FALSE);
        configProperties.add(property);

        // Let the user specify if the username should be prefixed with domain
        property = new ProviderConfigProperty();
        property.setName(ShortUsernameMapper.ConfigKey.USE_DOMAIN_AS_PREFIX);
        property.setLabel("Use domain as prefix?");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setHelpText("Enable this to include the domain part of the email as a prefix to the shortname. " +
                "You can use this in combination with a comma-separated list of domain names that should be excluded from " +
                "being used as prefixes.");
        property.setDefaultValue(Boolean.TRUE);
        configProperties.add(property);

        // Let the user specify a comma-separated list of domains that should not be used for prefixes
        property = new ProviderConfigProperty();
        property.setName(ShortUsernameMapper.ConfigKey.DOMAINS_NOT_USED_AS_PREFIX);
        property.setLabel("Domains not used as prefix");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Only relevant if 'Use domain as prefix' is true. This option allows you to specify a " +
                "comma-separated list of domains that should not be used as prefixes to the shortname. " +
                "E.g. You can specify your primary domain name so that only users coming from other domain will " +
                "receive a prefix. Leave this empty to prefix all usernames with domain.");
        configProperties.add(property);
    }

    public ShortUsernameMapper() {
        log.info("ShortUsernameMapper version " + BuildInfo.INSTANCE.getVersionAndBuildTimestamp());
    }

    @Override
    public String getDisplayCategory() {
        return "Token mapper";
    }

    @Override
    public String getDisplayType() {
        return "Dapla short username mapper" + " (v" + BuildInfo.INSTANCE.getVersion() + ")";
    }

    @Override
    public String getHelpText() {
        return "Transform a user's email to a short username claim (RFC-1123 compliant string). Example: john.doe@example.com -> example-john-doe";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    protected void setClaim(final IDToken token,
                            final ProtocolMapperModel mappingModel,
                            final UserSessionModel userSession,
                            final KeycloakSession keycloakSession,
                            final ClientSessionContext clientSessionCtx) {

        boolean verbose = isVerboseLoggingEnabled(mappingModel);
        String claimName = mappingModel.getConfig().get(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME);
        debugLog(verbose, "Map claim " + claimName);

        debugLog(verbose, "token: " + Json.prettyFrom(token));
        debugLog(verbose, "user session: " + Json.prettyFrom(token));

        String email = userSession.getUser().getEmail();
        if (email == null || email.trim().isEmpty()) {
            log.info("Email was null or empty. Unable to deduce shortname.");
            return;
        } else {
            debugLog(verbose, "Email is " + email);
        }

        boolean useDomainAsPrefix = useDomainAsPrefix(mappingModel);
        Set<String> domainsNotUsedAsPrefix = domainsNotUsedAsPrefix(mappingModel);
        try {
            String shortUsername = emailToShortUsername(email, useDomainAsPrefix, domainsNotUsedAsPrefix);
            debugLog(verbose, "Claim " + claimName + " set to " + shortUsername);
            OIDCAttributeMapperHelper.mapClaim(token, mappingModel, shortUsername);
        } catch (ShortUsernameMapperException e) {
            log.info("Could not set " + claimName + " claim: " + e.getMessage());
        }
    }

    static String emailToShortUsername(String email, boolean useDomainAsPrefix, Set<String> domainsNotUsedAsPrefix) throws ShortUsernameMapperException {
        String localPart = Email.localPart(email)
                .orElseThrow(() -> new ShortUsernameMapperException("Unable to retrieve local part from email " + email));

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
        return Boolean.parseBoolean(mappingModel.getConfig().get(ConfigKey.USE_DOMAIN_AS_PREFIX));
    }

    Set<String> domainsNotUsedAsPrefix(final ProtocolMapperModel mappingModel) {
        String config = mappingModel.getConfig().get(ConfigKey.DOMAINS_NOT_USED_AS_PREFIX);
        return (config == null || config.isBlank())
                ? Set.of()
                : Arrays.stream(config.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    boolean isVerboseLoggingEnabled(final ProtocolMapperModel mappingModel) {
        return Boolean.parseBoolean(mappingModel.getConfig().get(ConfigKey.VERBOSE_LOGGING));
    }

    private static String asRfc1123(String s) {
        return s.replaceAll("[^A-Za-z0-9]", "-").toLowerCase();
    }

    private void debugLog(boolean verbose, String msg) {
        if (verbose) {
            log.info(msg);
        } else {
            log.debug(msg);
        }
    }

    static class ShortUsernameMapperException extends Exception {
        public ShortUsernameMapperException(String message) {
            super(message);
        }
    }

}