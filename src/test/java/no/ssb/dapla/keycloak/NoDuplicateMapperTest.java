package no.ssb.dapla.keycloak;

import org.junit.jupiter.api.Test;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.provider.ProviderFactory;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class NoDuplicateMapperTest {

    @Test
    public void shouldNotHaveMappersWithDuplicateIds() {
        final ServiceLoader<ProtocolMapper> serviceLoader = ServiceLoader.load(ProtocolMapper.class);
        final List<String> mapperIds = StreamSupport.stream(serviceLoader.spliterator(), false)
                .map(ProviderFactory::getId)
                .toList();

        assertThat(mapperIds).doesNotHaveDuplicates();
    }
}