/**
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

import com.google.inject.Inject;
import sonia.scm.config.ConfigurationPermissions;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("v2/config/tagprotection")
public class TagProtectionConfigResource {
    private final TagProtectionConfigurationStore configurationStore;
    private final TagProtectionConfigMapper mapper;

    @Inject
    public TagProtectionConfigResource(TagProtectionConfigurationStore configurationStore, TagProtectionConfigMapper mapper) {

        this.configurationStore = configurationStore;
        this.mapper = mapper;
    }


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public TagProtectionConfigDto getConfig() {
        ConfigurationPermissions.read(Constants.NAME).check();
        return mapper.map(configurationStore.getConfiguration());
    }

    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setConfig(@Context UriInfo uriInfo, TagProtectionConfigDto config) {
        ConfigurationPermissions.write(Constants.NAME).check();
        configurationStore.saveConfiguration(mapper.map(config));

        return Response.noContent().build();
    }


}
