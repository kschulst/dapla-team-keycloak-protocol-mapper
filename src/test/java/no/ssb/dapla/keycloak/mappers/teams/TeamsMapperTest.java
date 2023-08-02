package no.ssb.dapla.keycloak.mappers.teams;

import no.ssb.dapla.keycloak.mappers.ConfigPropertyKey;
import no.ssb.dapla.keycloak.services.teamapi.DummyDaplaTeamApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.IDToken;
import org.mockito.Mockito;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TeamsMapperTest {

    private ProtocolMapperModel protocolMapperModel;
    private UserSessionModel userSessionModel;
    private IDToken idToken;
    private KeycloakSession keycloakSession;
    private ClientSessionContext clientSessionContext;
    private TeamsMapper teamsMapper;

    @BeforeEach
    void setUp() {
        protocolMapperModel = new ProtocolMapperModel();
        protocolMapperModel.setConfig(Map.of(
                ConfigPropertyKey.VERBOSE_LOGGING, Boolean.TRUE.toString(),
                TeamsMapper.ConfigPropertyKey.API_IMPL, DummyDaplaTeamApiService.NAME
        ));

        userSessionModel = Mockito.mock(UserSessionModel.class);  // assuming you are using mockito
        idToken = new IDToken();
        keycloakSession = Mockito.mock(KeycloakSession.class);  // assuming you are using mockito
        clientSessionContext = Mockito.mock(ClientSessionContext.class);  // assuming you are using mockito
        teamsMapper = new TeamsMapper();
    }

    @Test
    void testMapToClaimUsingDummyDaplaTeamApiService() {
        Object claim = teamsMapper.mapToClaim(idToken, protocolMapperModel, userSessionModel, keycloakSession, clientSessionContext);
        assertThat(claim).isNotNull();
        assertThat(claim).isInstanceOf(String.class);
        assertThat((String) claim).isEqualTo("""
                ["demo-enhjoern-æ","demo-enhjoern-ø"]""");
    }
}
