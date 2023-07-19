package no.ssb.dapla.keycloak;

public class DaplaKeycloakException extends RuntimeException {

    public DaplaKeycloakException(String message) {
        super(message);
    }

    public DaplaKeycloakException(String message, Throwable cause) {
        super(message, cause);
    }

}
