package no.ssb.dapla.keycloak.mappers;

public class CouldNotDeduceClaimException extends RuntimeException {

    public CouldNotDeduceClaimException(String message) {
        super(message);
    }

    public CouldNotDeduceClaimException(String message, Throwable cause) {
        super(message, cause);
    }

}
