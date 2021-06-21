package org.commonjava.util.sidecar.jaxrs;

import io.smallrye.mutiny.Uni;
import org.commonjava.util.sidecar.services.ArchiveRetrieveService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path( "/api/preSeed" )
public class PreSeedResource
{
    @Inject
    ArchiveRetrieveService archiveService;

    @Operation( description = "Retrieve historical archive by build config ID and extract locally" )
    @APIResponse( responseCode = "200", description = "Download and extract archive successfully" )
    @APIResponse( responseCode = "404", description = "Local archive is not available" )
    @Path( "archive/{buildConfigId}" )
    @GET
    public Uni<Response> get( @PathParam( "buildConfigId" ) final String buildConfigId ) throws Exception
    {
        Response response;
        if ( buildConfigId == null || buildConfigId.trim().isEmpty() )
        {
            response = Response.status( NOT_FOUND )
                               .type( MediaType.TEXT_PLAIN )
                               .entity( "Empty build config Id." )
                               .build();
            return Uni.createFrom().item( response );
        }
        boolean success = archiveService.decompressArchive( buildConfigId );
        if ( success )
        {
            response = Response.ok()
                               .type( MediaType.TEXT_PLAIN )
                               .entity( "Download and extract archive successfully." )
                               .build();
        }
        else
        {
            response = Response.status( NOT_FOUND )
                               .type( MediaType.TEXT_PLAIN )
                               .entity( "No archive available for such build config Id." )
                               .build();
        }
        return Uni.createFrom().item( response );
    }
}