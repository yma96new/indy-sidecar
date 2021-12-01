/**
 * Copyright (C) 2011-2021 Red Hat, Inc. (https://github.com/Commonjava/indy-sidecar)
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
package org.commonjava.util.sidecar.services;

import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import kotlin.Pair;
import org.apache.commons.io.IOUtils;
import org.commonjava.util.sidecar.config.ProxyConfiguration;
import org.commonjava.util.sidecar.interceptor.ExceptionHandler;
import org.commonjava.util.sidecar.model.AccessChannel;
import org.commonjava.util.sidecar.model.StoreEffect;
import org.commonjava.util.sidecar.model.StoreKey;
import org.commonjava.util.sidecar.model.StoreType;
import org.commonjava.util.sidecar.model.TrackedContentEntry;
import org.commonjava.util.sidecar.model.TrackingKey;
import org.commonjava.util.sidecar.util.OtelAdapter;
import org.commonjava.util.sidecar.util.ProxyStreamingOutput;
import org.commonjava.util.sidecar.util.UrlUtils;
import org.commonjava.util.sidecar.util.WebClientAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static io.vertx.core.http.HttpMethod.HEAD;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.commonjava.util.sidecar.services.PreSeedConstants.CONTENT_REST_BASE_PATH;
import static org.commonjava.util.sidecar.services.PreSeedConstants.FORBIDDEN_HEADERS;
import static org.commonjava.util.sidecar.util.SidecarUtils.getBuildConfigId;
import static org.commonjava.util.sidecar.util.SidecarUtils.normalizePathAnd;

@ApplicationScoped
@ExceptionHandler
public class ProxyService
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    ProxyConfiguration proxyConfiguration;

    @Inject
    Classifier classifier;

    @Inject
    OtelAdapter otel;

    @Inject
    ReportService reportService;

    public Uni<Response> doHead( String packageType, String type, String name, String path, HttpServerRequest request )
                    throws Exception
    {
        String contentPath = UrlUtils.buildUrl( CONTENT_REST_BASE_PATH, packageType, type, name, path );
        return doHead( contentPath, request );
    }

    public Uni<Response> doHead( String path, HttpServerRequest request ) throws Exception
    {
        return normalizePathAnd( path, p -> classifier.classifyAnd( p, request, ( client, service ) -> wrapAsyncCall(
                        client.head( p, request ).call(), request.method() ) ) );
    }

    public Uni<Response> doGet( String packageType, String type, String name, String path, HttpServerRequest request )
                    throws Exception
    {
        String contentPath = UrlUtils.buildUrl( CONTENT_REST_BASE_PATH, packageType, type, name, path );
        return doGet( contentPath, request );
    }

    public Uni<Response> doGet( String path, HttpServerRequest request ) throws Exception
    {
        TrackedContentEntry entry = null;
        if ( getBuildConfigId() != null )
        {
            entry = new TrackedContentEntry( new TrackingKey( getBuildConfigId() ), generateStoreKey( path ),
                                             AccessChannel.NATIVE, "",
                                             "/" + path.replaceFirst( "^\\/?(\\w+\\/){5}", "" ), StoreEffect.DOWNLOAD,
                                             (long) 0, "", "", "" );
        }
        TrackedContentEntry finalEntry = entry;
        return normalizePathAnd( path, p -> classifier.classifyAnd( p, request, ( client, service ) -> wrapAsyncCall(
                        client.get( p, request ).call(), request.method(), finalEntry ) ) );
    }

    public Uni<Response> doPost( String path, InputStream is, HttpServerRequest request ) throws Exception
    {
        return normalizePathAnd( path, p -> classifier.classifyAnd( p, request, ( client, service ) -> wrapAsyncCall(
                        client.post( p, is, request ).call(), request.method() ) ) );
    }

    public Uni<Response> doPut( String packageType, String type, String name, String path, InputStream is,
                                HttpServerRequest request ) throws Exception
    {
        String contentPath = UrlUtils.buildUrl( CONTENT_REST_BASE_PATH, packageType, type, name, path );
        return doPut( contentPath, is, request );
    }

    public Uni<Response> doPut( String path, InputStream is, HttpServerRequest request ) throws Exception
    {
        if ( getBuildConfigId() != null )
        {
            byte[] bytes = IOUtils.toByteArray( is );
            TrackedContentEntry entry =
                            new TrackedContentEntry( new TrackingKey( getBuildConfigId() ), generateStoreKey( path ),
                                                     AccessChannel.NATIVE,
                                                     "http://" + proxyConfiguration.getServices().iterator().next().host
                                                                     + "/" + path, path, StoreEffect.UPLOAD,
                                                     (long) bytes.length, "", "", "" );
            updateMessageDigest( bytes, entry );
            reportService.appendUpload( entry );
        }
        return normalizePathAnd( path, p -> classifier.classifyAnd( p, request, ( client, service ) -> wrapAsyncCall(
                        client.put( p, is, request ).call(), request.method() ) ) );
    }

    public Uni<Response> doDelete( String path, HttpServerRequest request ) throws Exception
    {
        return normalizePathAnd( path, p -> classifier.classifyAnd( p, request, ( client, service ) -> wrapAsyncCall(
                        client.delete( p ).headersFrom( request ).call(), request.method() ) ) );
    }

    public Uni<Response> wrapAsyncCall( WebClientAdapter.CallAdapter asyncCall, HttpMethod method )
    {
        return wrapAsyncCall( asyncCall, method, null );
    }

    public Uni<Response> wrapAsyncCall( WebClientAdapter.CallAdapter asyncCall, HttpMethod method,
                                        TrackedContentEntry entry )
    {
        Uni<Response> ret =
                        asyncCall.enqueue().onItem().transform( ( resp ) -> convertProxyResp( resp, method, entry ) );
        return ret.onFailure().recoverWithItem( this::handleProxyException );
    }

    /**
     * Send status 500 with error message body.
     * @param t error
     */
    Response handleProxyException( Throwable t )
    {
        logger.error( "Proxy error", t );
        return Response.status( INTERNAL_SERVER_ERROR ).entity( t + ". Caused by: " + t.getCause() ).build();
    }

    /**
     * Read status and headers from proxy resp and set them to direct response.
     * @param resp proxy resp
     */
    private Response convertProxyResp( okhttp3.Response resp, HttpMethod method, TrackedContentEntry entry )
    {
        logger.debug( "Proxy resp: {} {}", resp.code(), resp.message() );
        logger.trace( "Raw resp headers:\n{}", resp.headers() );
        Response.ResponseBuilder builder = Response.status( resp.code(), resp.message() );
        resp.headers().forEach( header -> {
            if ( isHeaderAllowed( header, method ) )
            {
                logger.debug( "Setting response header: {} = {}", header.getFirst(), header.getSecond() );
                builder.header( header.getFirst(), header.getSecond() );
            }
        } );
        if ( resp.body() != null && entry != null )
        {
            byte[] bytes = new byte[0];
            try
            {
                bytes = resp.body().bytes();
            }
            catch ( IOException e )
            {
                logger.error( "Failed to read bytes from okhttp response", e );
            }
            entry.setSize( (long) bytes.length );
            String[] headers = resp.header( "indy-origin" ).split( ":" );
            entry.setOriginUrl( "http://" + proxyConfiguration.getServices().iterator().next().host + "/api/content/"
                                                + headers[0] + "/" + headers[1] + "/" + headers[2] + entry.getPath() );
            updateMessageDigest( bytes, entry );
            reportService.appendDownload( entry );
        }
        builder.entity( new ProxyStreamingOutput( resp.body().byteStream(), otel ) );
        return builder.build();
    }

    private void updateMessageDigest( byte[] bytes, TrackedContentEntry entry )
    {
        MessageDigest message;
        try
        {
            message = MessageDigest.getInstance( "MD5" );
            message.update( bytes );
            entry.setMd5( DatatypeConverter.printHexBinary( message.digest() ).toLowerCase() );
            message = MessageDigest.getInstance( "SHA-1" );
            message.update( bytes );
            entry.setSha1( DatatypeConverter.printHexBinary( message.digest() ).toLowerCase() );
            message = MessageDigest.getInstance( "SHA-256" );
            message.update( bytes );
            entry.setSha256( DatatypeConverter.printHexBinary( message.digest() ).toLowerCase() );
        }
        catch ( NoSuchAlgorithmException e )
        {
            logger.warn( "Bytes hash calculation failed for request" );
        }
    }

    private boolean isHeaderAllowed( Pair<? extends String, ? extends String> header, HttpMethod method )
    {
        if ( method == HEAD )
        {
            return true;
        }
        String key = header.getFirst();
        return !FORBIDDEN_HEADERS.contains( key.toLowerCase() );
    }

    private StoreKey generateStoreKey( String path )
    {
        String[] elements = path.split( "/" );
        return new StoreKey( elements[2], StoreType.valueOf( elements[3] ), elements[4] );
    }

}