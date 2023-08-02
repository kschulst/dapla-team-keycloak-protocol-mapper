package no.ssb.dapla.keycloak;

import no.ssb.dapla.keycloak.services.teamapi.MockyDaplaTeamApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
class MockyDaplaTeamApiServiceIT {
    private MockyDaplaTeamApiService service;

    @BeforeEach
    public void setup() {
        service = new MockyDaplaTeamApiService("https://run.mocky.io/v3/b1e6cf15-337d-404d-8e34-4a2fd3fc3d74");
    }

    @Test
    public void testGetTeams() {
        List<String> expected = Arrays.asList("demo-enhjoern-x", "demo-enhjoern-y", "demo-enhjoern-z"); // Assuming these teams exist for the given API endpoint
        List<String> actual = service.getTeams();

        assertEquals(expected, actual);
    }
}