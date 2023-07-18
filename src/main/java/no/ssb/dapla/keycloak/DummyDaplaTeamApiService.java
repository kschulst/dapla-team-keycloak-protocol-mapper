package no.ssb.dapla.keycloak;

import org.jboss.logging.Logger;

import java.util.List;

public class DummyDaplaTeamApiService implements DaplaTeamApiService {
    private static final Logger log = Logger.getLogger(DummyDaplaTeamApiService.class);

    public DummyDaplaTeamApiService() {
        log.info("Using DummyDaplaTeamApiService");
    }

    @Override
    public List<String> getTeams() {
        return List.of(
                "demo-enhjoern-æ",
                "demo-enhjoern-ø"
        );
    }

    @Override
    public List<String> getGroups() {
        return List.of(
                "demo-enhjoern-æ-data-admins",
                "demo-enhjoern-æ-developers",
                "demo-enhjoern-ø-developers"
        );
    }
}
