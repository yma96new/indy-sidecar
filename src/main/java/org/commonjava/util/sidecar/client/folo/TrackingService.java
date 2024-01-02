/**
 * Copyright (C) 2022-2023 Red Hat, Inc. (https://github.com/Commonjava/indy-tracking-service)
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
package org.commonjava.util.sidecar.client.folo;

import org.commonjava.util.sidecar.model.TrackedContentEntry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path( "/api/folo/admin" )
@RegisterRestClient( configKey = "tracking-service-api" )
public interface TrackingService
{
    @PUT
    @Path( "/report/recordArtifact" )
    Response recordArtificat( final TrackedContentEntry contentEntry );
}