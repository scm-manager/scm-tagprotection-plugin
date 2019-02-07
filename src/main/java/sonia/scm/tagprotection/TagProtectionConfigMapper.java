package sonia.scm.tagprotection;

import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.BaseMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class TagProtectionConfigMapper extends BaseMapper {

    @Inject
    private ScmPathInfoStore scmPathInfoStore;

    public abstract TagProtectionConfigDto map(TagProtectionConfig config);
    public abstract TagProtectionConfig map(TagProtectionConfigDto dto);

    @AfterMapping
    public void addLinks(TagProtectionConfig source, @MappingTarget TagProtectionConfigDto target) {
        Links.Builder linksBuilder = linkingTo().self(globalSelf());
        if (ConfigurationPermissions.write(Constants.NAME).isPermitted()) {
            linksBuilder.single(Link.link("update", globalUpdate()));

        }
        target.add(linksBuilder.build());
    }

    private String globalSelf() {
        LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), TagProtectionConfigResource.class);
        return linkBuilder.method("getConfig").parameters().href();
    }

    private String globalUpdate() {
        LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), TagProtectionConfigResource.class);
        return linkBuilder.method("setConfig").parameters().href();
    }

}
