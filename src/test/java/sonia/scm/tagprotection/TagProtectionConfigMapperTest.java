package sonia.scm.tagprotection;


import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TagProtectionConfigMapperTest {

    private URI baseUri = URI.create("http://example.com/base/");

    private URI expectedBaseUri;

    @Rule
    public ShiroRule shiro = new ShiroRule();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ScmPathInfoStore scmPathInfoStore;

    @InjectMocks
    TagProtectionConfigMapperImpl mapper;

    @Before
    public void setUp() {
        when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
        expectedBaseUri = baseUri.resolve("v2/config/tagprotection/");
    }

    @Test
    @SubjectAware(username = "trillian",
            password = "secret",
            configuration = "classpath:sonia/scm/tagprotection/shiro.ini"
    )
    public void shouldMapAttributesToDto() {
        TagProtectionConfigDto dto = mapper.map(createConfig());
        assertThat(dto.getProtectionPattern()).isEqualTo("*?*");
        assertThat(dto.isReduceOwnerPrivilege()).isTrue();
    }

    @Test
    @SubjectAware(username = "trillian",
            password = "secret",
            configuration = "classpath:sonia/scm/tagprotection/shiro.ini"
    )
    public void shouldAddLinksToDto() {
        TagProtectionConfigDto dto = mapper.map(createConfig());
        assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo(expectedBaseUri.toString());
        assertThat(dto.getLinks().getLinkBy("update").get().getHref()).isEqualTo(expectedBaseUri.toString());
    }

    public void shouldMapAttributesFromDto() {
        TagProtectionConfig config = mapper.map(createDto());
        assertThat(config.getProtectionPattern()).isEqualTo("***");
        assertThat(config.isReduceOwnerPrivilege()).isFalse();
    }

    private TagProtectionConfig createConfig() {
        TagProtectionConfig config = new TagProtectionConfig();
        config.setProtectionPattern("*?*");
        config.setReduceOwnerPrivilege(true);
        return config;
    }

    private TagProtectionConfigDto createDto() {
        TagProtectionConfigDto dto = new TagProtectionConfigDto();
        dto.setProtectionPattern("***");
        dto.setReduceOwnerPrivilege(false);
        return dto;
    }
}