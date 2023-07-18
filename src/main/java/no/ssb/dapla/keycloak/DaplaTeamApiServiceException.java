package no.ssb.dapla.keycloak;

public class DaplaTeamApiServiceException extends RuntimeException {
    public DaplaTeamApiServiceException(String message) {
        super(message);
    }

    public DaplaTeamApiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
