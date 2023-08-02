package no.ssb.dapla.keycloak.services.teamapi;

import org.jboss.logging.Logger;

import java.util.List;

public class DummyDaplaTeamApiService implements DaplaTeamApiService {
    private static final Logger log = Logger.getLogger(DummyDaplaTeamApiService.class);
    public static final String NAME = "Dummy";

    public DummyDaplaTeamApiService() {
        log.debug("Using DummyDaplaTeamApiService");
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
