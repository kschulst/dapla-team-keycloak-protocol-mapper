package no.ssb.dapla.keycloak.mappers.teams;

import com.google.auto.service.AutoService;
import no.ssb.dapla.keycloak.DaplaKeycloakException;
import no.ssb.dapla.keycloak.mappers.AbstractTokenMapper;
import no.ssb.dapla.keycloak.mappers.ConfigPropertyType;
import no.ssb.dapla.keycloak.services.teamapi.DaplaTeamApiService;
import no.ssb.dapla.keycloak.services.teamapi.DummyDaplaTeamApiService;
import no.ssb.dapla.keycloak.services.teamapi.MockyDaplaTeamApiService;
import no.ssb.dapla.keycloak.utils.Json;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.representations.IDToken;

@AutoService(ProtocolMapper.class)
public class TeamsMapper extends AbstractTokenMapper {
    public static final String PROVIDER_ID = "oidc-dapla-teams-mapper";

    public static class ConfigPropertyKey {
        public static final String API_URL = "dapla.teams.team-api-url";
        public static final String API_IMPL = "dapla.teams.team-api-impl";
    }

    public TeamsMapper() {
        super(PROVIDER_ID,

                configProperty()
                        .name(ConfigPropertyKey.API_IMPL)
                        .label("Dapla Team API Impl")
                        .helpText("""
                                The API implementation.
                                Mocky: An online, mocked API.
                                Dummy: Offline, dummy replacement instead of a real API invocation.""")
                        .type(ConfigPropertyType.LIST)
                        .options(MockyDaplaTeamApiService.NAME, DummyDaplaTeamApiService.NAME)
                        .defaultValue(MockyDaplaTeamApiService.NAME)
                        .build(),

                configProperty()
                        .name(ConfigPropertyKey.API_URL)
                        .type(ConfigPropertyType.STRING)
                        .label("Dapla Team API URL")
                        .helpText("""
                                Specify the root URL for the Dapla Team API.
                                This is not relevant if 'Dapla Team API Impl' is Dummy.""")
                        .defaultValue("https://run.mocky.io")
                        .build()
                );

        // Allow the mapper to configure the target value to be "JSON" (required)
        OIDCAttributeMapperHelper.addJsonTypeConfig(configProperties);
    }

    @Override
    protected String helpText() {
        return "Adds a 'teams' claim, retrieved from Dapla Team API";
    }

    @Override
    protected Object mapToClaim(IDToken token, ProtocolMapperModel model, UserSessionModel userSession, KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        debugLog(model,"Retrieve Dapla teams");
        DaplaTeamApiService teamApiService = teamApiService(model);
        return Json.from(teamApiService.getTeams());
    }

    DaplaTeamApiService teamApiService(ProtocolMapperModel model) {
        String apiImpl = getConfigString(model, ConfigPropertyKey.API_IMPL);
        debugLog(model, "Use " + apiImpl + " Dapla Team API implementation");

        if (MockyDaplaTeamApiService.NAME.equals(apiImpl)) {
            String apiUrl = getConfigString(model, ConfigPropertyKey.API_URL);
            debugLog(model, "Dapla Team API url: " + apiUrl);
            return new MockyDaplaTeamApiService(apiUrl);
        }
        else if (DummyDaplaTeamApiService.NAME.equals(apiImpl)) {
            return new DummyDaplaTeamApiService();
        }
        else {
            throw new DaplaKeycloakException("Unsupported Team API implementation: " + apiImpl);
        }
    }

}
