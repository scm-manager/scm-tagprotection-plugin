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

import com.google.inject.Inject;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.web.VndMediaType;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@OpenAPIDefinition(tags = {
  @Tag(name = "Tag Protection Plugin", description = "Tag Protection plugin provided endpoints")
})
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
  @Operation(summary = "Get global tag protection configuration", description = "Returns the global tag protection configuration.", tags = "Tag Protection Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = MediaType.APPLICATION_JSON,
      schema = @Schema(implementation = TagProtectionConfigDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:read:tagprotection\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public TagProtectionConfigDto getConfig() {
    ConfigurationPermissions.read(Constants.NAME).check();
    return mapper.map(configurationStore.getConfiguration());
  }

  @PUT
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(summary = "Set global tag protection configuration", description = "Sets the global tag protection configuration.", tags = "Tag Protection Plugin")
  @ApiResponse(responseCode = "204", description = "no content")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"configuration:write:tagprotection\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response setConfig(@Context UriInfo uriInfo, TagProtectionConfigDto config) {
    ConfigurationPermissions.write(Constants.NAME).check();
    configurationStore.saveConfiguration(mapper.map(config));

    return Response.noContent().build();
  }


}
