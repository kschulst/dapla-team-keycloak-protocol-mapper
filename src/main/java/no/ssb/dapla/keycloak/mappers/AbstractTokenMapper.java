package no.ssb.dapla.keycloak.mappers;

import com.google.common.base.CaseFormat;
import no.ssb.dapla.keycloak.BuildInfo;
import no.ssb.dapla.keycloak.utils.Converter;
import no.ssb.dapla.keycloak.utils.Json;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractTokenMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private final String providerId;
    protected final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    protected final Logger log;

    public AbstractTokenMapper(String providerId, ProviderConfigProperty... additionalConfigProperties) {
        this(providerId, Arrays.asList(additionalConfigProperties));
    }

    public AbstractTokenMapper(String providerId, List<ProviderConfigProperty> additionalConfigProperties) {
        this.log = Logger.getLogger(getClass());

        if (providerId == null || ! providerId.startsWith("oidc-dapla")) {
            throw new IllegalArgumentException("Claim mapper id must start with 'oidc-dapla'");
        }
        this.providerId = providerId;

        // Let the user define under which claim name (key) the protocol mapper writes its value.
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);

        // Let the user define for which tokens the protocol mapper is executed (access token,
        // id token, user info).
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, getClass());

        this.configProperties.add(configProperty()
                .name(ConfigPropertyKey.VERBOSE_LOGGING)
                .label("Verbose logging")
                .helpText("""
                        Enable this to include extra verbose logging in the Keycloak application logs.
                        This can be used for pinpointing problems without having to adjust the Keycloak logging config.""")
                .type(ConfigPropertyType.BOOLEAN)
                .defaultValue(Boolean.FALSE)
                .build());

        this.configProperties.addAll(additionalConfigProperties);
    }

    protected static ConfigPropertyBuilder configProperty() {
        return new ConfigPropertyBuilder();
    }

    @Override
    public String getDisplayCategory() {
        return "Token mapper";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getId() {
        return providerId;
    }

    @Override
    public String getDisplayType() {
        return getDisplayNameFromId() + " (v" + BuildInfo.INSTANCE.getVersion() + ")";
    }

    private String getDisplayNameFromId() {
        // remove 'oidc-' prefix
        String words = getId().replaceFirst("oidc-", "");

        // convert from LOWER_HYPHEN to UPPER_CAMEL and then to LOWER_SPACE
        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, words)
                .replaceAll("([A-Z])", " $1")
                .trim();
    }

    protected abstract String helpText();

    @Override
    public String getHelpText() {
        return helpText();
    }

    protected abstract Object mapToClaim(final IDToken token,
                                         final ProtocolMapperModel model,
                                         final UserSessionModel userSession,
                                         final KeycloakSession keycloakSession,
                                         final ClientSessionContext clientSessionCtx);

    @Override
    protected void setClaim(final IDToken token,
                            final ProtocolMapperModel mappingModel,
                            final UserSessionModel userSession,
                            final KeycloakSession keycloakSession,
                            final ClientSessionContext clientSessionCtx) {
        boolean verbose = isVerboseLoggingEnabled(mappingModel);
        String claimName = mappingModel.getConfig().get(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME);
        debugLog(verbose, "Map claim " + claimName);
        debugLog(verbose, "Token: " + Json.prettyFrom(token));
        //debugLog(verbose, "User session: " + Json.prettyFrom(userSession));

        try {
            Object claimValue = mapToClaim(token, mappingModel, userSession, keycloakSession, clientSessionCtx);
            debugLog(verbose, "Claim " + claimName + " set to " + claimValue);
            OIDCAttributeMapperHelper.mapClaim(token, mappingModel, claimValue);
        }
        catch (CouldNotDeduceClaimException e) {
            debugLog(verbose,"Unable to deduce " + claimName + " claim value. Token was NOT populated.", e);
        }
    }

    protected boolean isVerboseLoggingEnabled(final ProtocolMapperModel mappingModel) {
        return getConfigBoolean(mappingModel, ConfigPropertyKey.VERBOSE_LOGGING);
    }

    protected void debugLog(final ProtocolMapperModel mappingModel, String msg) {
        debugLog(isVerboseLoggingEnabled(mappingModel), msg);
    }

    protected void debugLog(final ProtocolMapperModel mappingModel, String msg, Throwable t) {
        debugLog(isVerboseLoggingEnabled(mappingModel), msg, t);
    }

    protected void debugLog(boolean verbose, String msg) {
        debugLog(verbose, msg, null);
    }

    protected void debugLog(boolean verbose, String msg, Throwable t) {
        if (verbose) {
            log.info(msg, t);
        } else {
            log.debug(msg, t);
        }
    }

    protected <T> T getConfig(ProtocolMapperModel mappingModel, String configKey, Class<T> type) {
        return Converter.convert(mappingModel.getConfig().get(configKey), type);
    }

    protected String getConfigString(ProtocolMapperModel mappingModel, String configKey) {
        return getConfig(mappingModel, configKey, String.class);
    }

    protected Boolean getConfigBoolean(ProtocolMapperModel mappingModel, String configKey) {
        return getConfig(mappingModel, configKey, Boolean.class);
    }

    protected Integer getConfigInteger(ProtocolMapperModel mappingModel, String configKey) {
        return getConfig(mappingModel, configKey, Integer.class);
    }

}
