/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
