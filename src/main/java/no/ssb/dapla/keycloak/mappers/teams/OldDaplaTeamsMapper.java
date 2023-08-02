package no.ssb.dapla.keycloak.mappers.teams;

import com.google.auto.service.AutoService;
import no.ssb.dapla.keycloak.BuildInfo;
import no.ssb.dapla.keycloak.DaplaKeycloakException;
import no.ssb.dapla.keycloak.services.teamapi.DaplaTeamApiService;
import no.ssb.dapla.keycloak.services.teamapi.DummyDaplaTeamApiService;
import no.ssb.dapla.keycloak.services.teamapi.MockyDaplaTeamApiService;
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
import java.util.List;

/*
 * DaplaTeamsMapper is a Keycloak protocol mapper that looks up a user's associated
 * Dapla teams and populates a JWT token claim.
 */
@AutoService(ProtocolMapper.class)
public class OldDaplaTeamsMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static class ConfigKey {
        public static final String API_URL = "dapla-team-api.url";
        public static final String API_IMPL = "dapla-team-api.impl";
    }

    private static final Logger log = Logger.getLogger(OldDaplaTeamsMapper.class);

    /*
     * A config which keycloak uses to display a generic dialog to configure the token.
     */
    static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    /*
     * The ID of the token mapper. Is public, because we need this id in our data-setup project to
     * configure the protocol mapper in keycloak.
     */
    public static final String PROVIDER_ID = "oidc-dapla-teams-mapper";

    static {
        ProviderConfigProperty property;

        // Let the user define under which claim name (key) the protocol mapper writes its value.
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);

        // Let the user specify the JSON type that value should be mapped to. Must be "JSON".
        OIDCAttributeMapperHelper.addJsonTypeConfig(configProperties);

        // Let the user define for which tokens the protocol mapper is executed (access token,
        // id token, user info).
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, OldDaplaTeamsMapper.class);

        // Let the user decide if we should use a dummy service instead of a real API request for Dapla Team API
        property = new ProviderConfigProperty();
        property.setName(ConfigKey.API_IMPL);
        property.setLabel("Dapla Team API Impl");
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(List.of(MockyDaplaTeamApiService.NAME, DummyDaplaTeamApiService.NAME));
        property.setHelpText("The API implementation. Select Mocky to use an online, mocked API. Select Dummy to use an offline, dummy replacement instead of a real API invocation.");
        property.setDefaultValue(MockyDaplaTeamApiService.NAME);
        configProperties.add(property);

        // Let the user specify the URL for Dapla Team API
        property = new ProviderConfigProperty();
        property.setName(ConfigKey.API_URL);
        property.setLabel("Dapla Team API URL");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Specify the root URL for the Dapla Team API. Not used if 'Dapla Team API Impl' is Dummy.");
        //property.setDefaultValue("https://team-api.dapla.ssb.no");
        property.setDefaultValue("https://run.mocky.io");
        configProperties.add(property);
    }

    public OldDaplaTeamsMapper() {
        log.debug("DaplaTeamsMapper version " + BuildInfo.INSTANCE.getVersionAndBuildTimestamp());
    }

    @Override
    public String getDisplayCategory() {
        return "Token mapper";
    }

    @Override
    public String getDisplayType() {
        return "Dapla Team API mapper" + " (v" + BuildInfo.INSTANCE.getVersion() + ")";
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

        log.debug("Retrieve Dapla teams");
        DaplaTeamApiService teamApiService = teamApiService(mappingModel);
        String teamsJson = Json.from(teamApiService.getTeams());
        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, teamsJson);
    }

    DaplaTeamApiService teamApiService(ProtocolMapperModel mappingModel) {
        String impl = mappingModel.getConfig().get(ConfigKey.API_IMPL);
        log.debug("Using " + impl + " Dapla Team API implementation");

        if (MockyDaplaTeamApiService.NAME.equals(impl)) {
            String apiUrl = mappingModel.getConfig().get(ConfigKey.API_URL);
            log.debug("Dapla Team API url: " + apiUrl);
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