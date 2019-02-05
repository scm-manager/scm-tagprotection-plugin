package sonia.scm.tagprotection;

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
@Enrich(Index.class)
public class TagProtectionIndexLinkEnricher implements HalEnricher {
    private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

    @Inject
    public TagProtectionIndexLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
        this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
    }

    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {
        if (ConfigurationPermissions.read(Constants.NAME).isPermitted()) {
            appender.appendLink("tagProtection", createLink());
        }
    }

    private String createLink() {
        return new LinkBuilder(scmPathInfoStoreProvider.get().get(),
                TagProtectionConfigResource.class)
                .method("getConfig")
                .parameters()
                .href();
    }
}
