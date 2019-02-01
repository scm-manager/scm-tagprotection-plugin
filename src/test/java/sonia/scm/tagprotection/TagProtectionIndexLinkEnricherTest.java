package sonia.scm.tagprotection;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.inject.util.Providers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.LinkAppender;
import sonia.scm.api.v2.resources.LinkEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import javax.inject.Provider;
import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class TagProtectionIndexLinkEnricherTest {

    @Rule
    public ShiroRule shiro = new ShiroRule();

    @Mock
    private LinkAppender appender;

    private TagProtectionIndexLinkEnricher enricher;

    @Before
    public void setUp() {
        ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
        scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
        Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
        enricher = new TagProtectionIndexLinkEnricher(scmPathInfoStoreProvider);
    }

    @SubjectAware(
            username = "trillian",
            password = "secret",
            configuration = "classpath:sonia/scm/tagprotection/shiro.ini"
    )
    @Test
    public void shouldEnrich() {
        enricher.enrich(LinkEnricherContext.of(), appender);
        verify(appender).appendOne("tagProtection", "https://scm-manager.org/scm/api/v2/config/tagprotection/");
    }

    @SubjectAware(
            username = "unpriv",
            password = "secret",
            configuration = "classpath:sonia/scm/tagprotection/shiro.ini"
    )
    @Test
    public void shouldNotEnrichIfNotPermitted() {
        enricher.enrich(LinkEnricherContext.of(), appender);
        verifyZeroInteractions(appender);
    }
}
