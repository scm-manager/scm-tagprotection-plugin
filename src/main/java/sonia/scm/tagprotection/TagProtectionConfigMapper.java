/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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

import jakarta.inject.Inject;

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
