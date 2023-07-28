package no.ssb.dapla.keycloak.utils;

import java.util.Optional;

public class Email {

    private static final String EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    public static Optional<String> localPart(String email) {
        if (email == null || email.trim().isEmpty() || !email.matches(EMAIL_REGEX)) {
            return Optional.empty();
        }

        return Optional.of(email
                .trim()
                .substring(0, email.indexOf('@'))
                .replaceAll("[^A-Za-z0-9]", "_") // Replace any illegal characters
        );
    }

    public static Optional<String> domainPart(String email) {
        if (email == null || email.trim().isEmpty() || !email.matches(EMAIL_REGEX)) {
            return Optional.empty();
        }

        return Optional.of(email.trim().substring(email.indexOf('@') + 1));
    }

    public static Optional<String> domainPartWithoutTld(String email) {
        return domainPart(email).map(domain -> domain.substring(0, domain.lastIndexOf('.')));
    }

}