package no.ssb.dapla.keycloak;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class MockyDaplaTeamApiService implements DaplaTeamApiService {

    public static final String NAME = "Mocky";
    public static final String TEAMS_ENDPOINT = "v3/b1e6cf15-337d-404d-8e34-4a2fd3fc3d74";

    private static final Logger log = Logger.getLogger(MockyDaplaTeamApiService.class);
    private final OkHttpClient httpClient = new OkHttpClient();
    private final URI teamApiUrl;

    public MockyDaplaTeamApiService(String teamApiUrl) {
        this.teamApiUrl = URI.create(teamApiUrl);
        log.info("Using MockyDaplaTeamApiService (" + teamApiUrl + ")");
    }

    @Override
    public List<String> getTeams() {
        Request request = new Request.Builder()
                .url(teamApiUrl.resolve(TEAMS_ENDPOINT).toString())
                .build();
        log.debug("Request: " + request);
        try (Response response = httpClient.newCall(request).execute()) {
            log.debug("Response: " + response);

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            log.debug("Response body: " + response.body());

            TeamsWrapper res = Json.adapter(TeamsWrapper.class).fromJson(response.body().source());
            return res.teams;
        }
        catch (Exception e) {
            throw new DaplaKeycloakException("Error fetching teams from " + teamApiUrl, e);
        }
    }

    @Override
    public List<String> getGroups() {
        throw new UnsupportedOperationException("Retrieving a user's access groups is not yet implemented");
    }

    static class TeamsWrapper {
        List<String> teams;
    }

}
