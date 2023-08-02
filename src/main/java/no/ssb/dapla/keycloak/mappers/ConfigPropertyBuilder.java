package no.ssb.dapla.keycloak.mappers;

import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;

public class ConfigPropertyBuilder {

    final ProviderConfigProperty propertyConfig;

    public ConfigPropertyBuilder() {
        propertyConfig = new ProviderConfigProperty();
    }

    public ConfigPropertyBuilder name(String name) {
        propertyConfig.setName(name);
        return this;
    }

    public ConfigPropertyBuilder label(String label) {
        propertyConfig.setLabel(label);
        return this;
    }

    public ConfigPropertyBuilder helpText(String helpText) {
        propertyConfig.setHelpText(helpText);
        return this;
    }

    public ConfigPropertyBuilder type(ConfigPropertyType type) {
        propertyConfig.setType(type.stringValue());
        return this;
    }

    public ConfigPropertyBuilder options(List<String> options) {
        propertyConfig.setOptions(options);
        return this;
    }

    public ConfigPropertyBuilder options(String... options) {
        propertyConfig.setOptions(Arrays.asList(options));
        return this;
    }

    public ConfigPropertyBuilder defaultValue(Object defaultValue) {
        propertyConfig.setDefaultValue(defaultValue);
        return this;
    }

    public ConfigPropertyBuilder isSecret(boolean secret) {
        propertyConfig.setSecret(secret);
        return this;
    }

    public ConfigPropertyBuilder isReadOnly(boolean readOnly) {
        propertyConfig.setSecret(readOnly);
        return this;
    }

    public ProviderConfigProperty build() {
        requireValueSet(propertyConfig.getName(), "name");
        requireValueSet(propertyConfig.getType(), "type");
        requireValueSet(propertyConfig.getLabel(), "label");
        if (isListType() && (propertyConfig.getOptions() == null || propertyConfig.getOptions().isEmpty())) {
            throw new IllegalArgumentException("list type requires options");
        }

        return propertyConfig;
    }

    private static void requireValueSet(String value, String valueName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(valueName + " is required");
        }
    }

    private boolean isType(ConfigPropertyType type) {
        return type.stringValue().equals(propertyConfig.getType());
    }

    private boolean isListType() {
        return isType(ConfigPropertyType.LIST) || isType(ConfigPropertyType.MULTIVALUED_LIST);
    }

}
