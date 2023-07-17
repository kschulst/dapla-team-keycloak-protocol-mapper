package no.ssb.dapla.keycloak;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.representations.IDToken;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DaplaTeamsMapperTest {

    private DaplaTeamsMapper daplaTeamsMapper;
    private ProtocolMapperModel protocolMapperModel;
    private IDToken idToken;

    @BeforeEach
    public void setUp() {
        daplaTeamsMapper = new DaplaTeamsMapper();
        protocolMapperModel = new ProtocolMapperModel();
        Map<String, String> config = new HashMap<>();
        config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, "teams");
        protocolMapperModel.setConfig(config);
        idToken = new IDToken();
    }

    @Test
    public void testSetClaim() {
        daplaTeamsMapper.setClaim(idToken, protocolMapperModel, mock(UserSessionModel.class),
                mock(KeycloakSession.class), mock(ClientSessionContext.class));
        assertThat(idToken.getOtherClaims()).containsKey("teams");
        assertThat(idToken.getOtherClaims().get("teams")).isEqualTo("demo-enhjoern-x");
    }

    @Test
    public void testGetConfigProperties() {
        assertThat(daplaTeamsMapper.getConfigProperties()).isEqualTo(DaplaTeamsMapper.configProperties);
    }

    @Test
    public void testGetId() {
        assertThat(daplaTeamsMapper.getId()).isEqualTo(DaplaTeamsMapper.PROVIDER_ID);
    }
}
