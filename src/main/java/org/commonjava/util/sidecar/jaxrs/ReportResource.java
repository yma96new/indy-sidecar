/**
 * Copyright (C) 2011-2022 Red Hat, Inc. (https://github.com/Commonjava/indy-sidecar)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.util.sidecar.jaxrs;

import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import org.commonjava.util.sidecar.model.TrackedContent;
import org.commonjava.util.sidecar.services.ReportService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

//Change path when Newcastle build complete signal API is integrated
@Path( "/api/folo/track/{id}/record" )
public class ReportResource
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    ReportService reportService;

    @Operation( description = "Retrieve tracking report content from memory" )
    @APIResponse( responseCode = "200", description = "Tracking report content" )
    @APIResponse( responseCode = "500", description = "Tracking report generation failed" )
    @Produces( APPLICATION_JSON )
    @GET
    public TrackedContent get()
    {
        return reportService.getTrackedContent(); //this will automatically serialized by jackson
    }

    @Operation( description = "Import tracking report content to configured indy" )
    @APIResponse( responseCode = "201", description = "Tracking report imported" )
    @APIResponse( responseCode = "404", description = "No tracking report found" )
    @Path( "/import" )
    @Produces( TEXT_PLAIN )
    @PUT
    public Uni<Response> importReport( final @Context HttpServerRequest request ) throws Exception
    {
        return reportService.importReport( request );
    }

    @Operation( description = "Delete tracking report content from memory" )
    @APIResponse( responseCode = "200", description = "Tracking report deleted" )
    @APIResponse( responseCode = "404", description = "No tracking report found" )
    @Produces( APPLICATION_JSON )
    @DELETE
    public TrackedContent delete()
    {
        reportService.clearReport();
        return reportService.getTrackedContent();
    }
}
