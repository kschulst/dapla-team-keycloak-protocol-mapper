package no.ssb.dapla.keycloak;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.List;

public class MockyDaplaTeamApiService implements DaplaTeamApiService {
    private static final Logger log = Logger.getLogger(MockyDaplaTeamApiService.class);
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Moshi moshi = new Moshi.Builder().build();
    private final JsonAdapter<TeamsWrapper> teamsJsonAdapter = moshi.adapter(TeamsWrapper.class);
    private final String teamApiUrl;

    public MockyDaplaTeamApiService(String teamApiUrl) {
        this.teamApiUrl = teamApiUrl;
        log.info("Using MockyDaplaTeamApiService (" + teamApiUrl + ")");
    }

    @Override
    public List<String> getTeams() {
        Request request = new Request.Builder()
                .url(teamApiUrl)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            TeamsWrapper res = teamsJsonAdapter.fromJson(response.body().source());

            return res.teams;
        }
        catch (Exception e) {
            throw new DaplaTeamApiServiceException("Error fetching teams from " + teamApiUrl, e);
        }
    }

    @Override
    public List<String> getGroups() {
        return null;
    }

    static class TeamsWrapper {
        List<String> teams;
    }

}
