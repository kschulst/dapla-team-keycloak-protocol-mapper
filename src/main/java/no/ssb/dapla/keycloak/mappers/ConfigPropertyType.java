package no.ssb.dapla.keycloak.mappers;

import org.keycloak.provider.ProviderConfigProperty;

public enum ConfigPropertyType {

    BOOLEAN(ProviderConfigProperty.BOOLEAN_TYPE),
    STRING(ProviderConfigProperty.STRING_TYPE),
    MULTIVALUED_STRING(ProviderConfigProperty.MULTIVALUED_STRING_TYPE),
    TEXT(ProviderConfigProperty.TEXT_TYPE),
    LIST(ProviderConfigProperty.LIST_TYPE),
    MULTIVALUED_LIST(ProviderConfigProperty.MULTIVALUED_LIST_TYPE),
    MAP(ProviderConfigProperty.MAP_TYPE),
    PASSWORD(ProviderConfigProperty.PASSWORD),
    SCRIPT(ProviderConfigProperty.SCRIPT_TYPE),
    FILE(ProviderConfigProperty.FILE_TYPE),
    ROLE(ProviderConfigProperty.ROLE_TYPE),
    GROUP(ProviderConfigProperty.GROUP_TYPE),
    CLIENT_LIST(ProviderConfigProperty.CLIENT_LIST_TYPE)
    ;


    private final String stringValue;

    ConfigPropertyType(String stringValue) {
        this.stringValue = stringValue;
    }

    public String stringValue() {
        return stringValue;
    }

    public static ConfigPropertyType fromStringValue(String stringValue) {
        for (ConfigPropertyType value : ConfigPropertyType.values()) {
            if (value.stringValue.equals(stringValue)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No enum constant present for the specified string value");
    }
}