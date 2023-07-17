package no.ssb.dapla.keycloak;

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
 * Keycloak protocol mapper that looks up a user's associated Dapla teams and populates
 * a JWT token claim.
 */
public class DaplaTeamsMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    /*
     * A config which keycloak uses to display a generic dialog to configure the token.
     */
    static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    /*
     * The ID of the token mapper. Is public, because we need this id in our data-setup project to
     * configure the protocol mapper in keycloak.
     */
    public static final String PROVIDER_ID = "dapla-teams-mapper";

    static {
        // Let the user define under which claim name (key) the protocol mapper writes its value.
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);

        // Let the user define for which tokens the protocol mapper is executed (access token,
        // id token, user info).
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, DaplaTeamsMapper.class);
    }

    @Override
    public String getDisplayCategory() {
        return "Dapla teams token mapper";
    }

    @Override
    public String getDisplayType() {
        return "Dapla Teams Mapper";
    }

    @Override
    public String getHelpText() {
        return "Retrieve the user's Dapla teams from dapla-team-api and add claim";
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
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, "demo-enhjoern-x");
    }

}