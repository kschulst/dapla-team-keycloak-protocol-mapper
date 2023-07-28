package no.ssb.dapla.keycloak.mapper.shortname;

import no.ssb.dapla.keycloak.*;
import no.ssb.dapla.keycloak.mapper.teams.DaplaTeamsMapper;
import no.ssb.dapla.keycloak.utils.Email;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.List;

/*
 * ShortnameMapper is a Keycloak protocol mapper that looks up a user's associated email and transforms it to
 * a [RFC-1123](https://datatracker.ietf.org/doc/html/rfc1123) compliant name.
 */
public class ShortUsernameMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static class ConfigKey {
        public static final String USE_DOMAIN_AS_PREFIX = "short-username.use-domain-as-prefix";
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

        // Let the user specify the URL for Dapla Team API
        property = new ProviderConfigProperty();
        property = new ProviderConfigProperty();
        property.setName(ShortUsernameMapper.ConfigKey.USE_DOMAIN_AS_PREFIX);
        property.setLabel("Use domain as prefix?");
        property.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        property.setHelpText("Enable this to include the domain part of the email as a prefix to the shortname");
        property.setDefaultValue(Boolean.TRUE);
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
        String email = token.getEmail();
        log.info("Map email " + email + " to shortname");
        if (email == null || email.trim().isEmpty()) {
            log.info("Email was null or empty. Unable to deduce shortname.");
            return;
        }

        boolean useDomainAsPrefix = Boolean.parseBoolean(mappingModel.getConfig().get(ConfigKey.USE_DOMAIN_AS_PREFIX));

        String shortUsername = emailToShortUsername(email, useDomainAsPrefix);
        log.info("Short username is " + shortUsername);
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, shortUsername);
    }

    static String emailToShortUsername(String email, boolean useDomainAsPrefix) {
        final String shortUsername;

        String localPart = Email.localPart(email).orElse("");
        if (localPart.isEmpty()) {
            return null;
        }

        if (useDomainAsPrefix) {
            String domainPart = Email.domainPartWithoutTld(email).orElse("");
            if (domainPart.isEmpty()) {
                return null;
            }
            return asRfc1123(domainPart + "-" + localPart);
        }
        else {
            return asRfc1123(localPart);
        }
    }

    private static String asRfc1123(String s) {
        return s.replaceAll("[^A-Za-z0-9]", "-").toLowerCase();
    }

}