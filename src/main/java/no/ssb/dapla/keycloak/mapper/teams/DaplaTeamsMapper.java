package no.ssb.dapla.keycloak.mapper.teams;

import no.ssb.dapla.keycloak.*;
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
 * Keycloak protocol mapper that looks up a user's associated Dapla teams and populates
 * a JWT token claim.
 */
public class DaplaTeamsMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final Logger log = Logger.getLogger(DaplaTeamsMapper.class);

    public static final String API_URL_CONFIG = "dapla-team-api.url";
    public static final String API_IMPL_CONFIG = "dapla-team-api.impl";

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
        ProviderConfigProperty property;

        // Let the user define under which claim name (key) the protocol mapper writes its value.
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);

        // Let the user define for which tokens the protocol mapper is executed (access token,
        // id token, user info).
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, DaplaTeamsMapper.class);

        // Let the user decide if we should use a dummy service instead of a real API request for Dapla Team API
        property = new ProviderConfigProperty();
        property.setName(API_IMPL_CONFIG);
        property.setLabel("Dapla Team API Impl");
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(List.of(MockyDaplaTeamApiService.NAME, DummyDaplaTeamApiService.NAME));
        property.setHelpText("The API implementation. Select Mocky to use an online, mocked API. Select Dummy to use an offline, dummy replacement instead of a real API invocation.");
        property.setDefaultValue(MockyDaplaTeamApiService.NAME);
        configProperties.add(property);

        // Let the user specify the URL for Dapla Team API
        property = new ProviderConfigProperty();
        property = new ProviderConfigProperty();
        property.setName(API_URL_CONFIG);
        property.setLabel("Dapla Team API URL");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Specify the root URL for the Dapla Team API. Not used if 'Dummy Dapla Team API' is enabled.`");
        //property.setDefaultValue("https://team-api.dapla.ssb.no");
        property.setDefaultValue("https://run.mocky.io");
        configProperties.add(property);
    }

    @Override
    public String getDisplayCategory() {
        return "Token mapper";
    }

    @Override
    public String getDisplayType() {
        return "Dapla Team API mapper";
    }

    @Override
    public String getHelpText() {
        return "Retrieve the user's Dapla teams from Dapla Team API and add claim";
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

        log.info("Retrieve Dapla teams");
        DaplaTeamApiService teamApiService = teamApiService(mappingModel);
        //String teamsJson = Json.from(teamApiService.getTeams());
        String teamsJson = "[\"demo-enhjoern-a\",\"demo-enhjoern-c\"]";
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, teamsJson);
    }

    DaplaTeamApiService teamApiService(ProtocolMapperModel mappingModel) {
        String impl = mappingModel.getConfig().get(API_IMPL_CONFIG);
        log.info("Using " + impl + " Dapla Team API implementation");

        if (MockyDaplaTeamApiService.NAME.equals(impl)) {
            String apiUrl = mappingModel.getConfig().get(API_URL_CONFIG);
            log.info("Dapla Team API url: " + apiUrl);
            return new MockyDaplaTeamApiService(apiUrl);
        }
        else if (DummyDaplaTeamApiService.NAME.equals(impl)) {
            return new DummyDaplaTeamApiService();
        }
        else {
            throw new DaplaKeycloakException("Unsupported Team API implementation: " + impl);
        }
    }

}