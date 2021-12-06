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

import org.apache.commons.io.IOUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.commonjava.util.sidecar.config.SidecarConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.commonjava.util.sidecar.services.PreSeedConstants.DEFAULT_REPO_PATH;

@ApplicationScoped
public class ArchiveRetrieveService
{

    private final static int CONNECTION_REQUEST_TIMEOUT = 30 * 60 * 1000; // 30m

    private final static int CONNECTION_TIMEOUT = 30 * 60 * 1000; // 30m

    private final static int SOCKET_TIMEOUT = 30 * 60 * 1000; // 30m

    private final static String BUILD_CONFIG_ID = "build.config.id";

    private final static String MAVEN_META = "maven-metadata.xml";

    private final static String NPM_META = "package.json";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    SidecarConfig sidecarConfig;

    private CloseableHttpClient client;

    @PostConstruct
    public void init()
    {
        final PoolingHttpClientConnectionManager ccm = new PoolingHttpClientConnectionManager();
        ccm.setMaxTotal( 30 );
        RequestConfig rc = RequestConfig.custom()
                                        .setConnectionRequestTimeout( CONNECTION_REQUEST_TIMEOUT )
                                        .setConnectTimeout( CONNECTION_TIMEOUT )
                                        .setSocketTimeout( SOCKET_TIMEOUT )
                                        .build();
        final HttpClientBuilder builder = HttpClients.custom()
                                                     .setConnectionManager( ccm )
                                                     .setDefaultRequestConfig( rc )
                                                     .setRetryHandler( ( exception, executionCount, context ) -> {
                                                         if ( executionCount > 3 )
                                                         {
                                                             return false;
                                                         }
                                                         if ( exception instanceof NoHttpResponseException )
                                                         {
                                                             logger.info( "NoHttpResponse start to retry times:"
                                                                                          + executionCount );
                                                             return true;
                                                         }
                                                         if ( exception instanceof SocketTimeoutException )
                                                         {
                                                             logger.info( "SocketTimeout start to retry times:"
                                                                                          + executionCount );
                                                             return true;
                                                         }
                                                         if ( exception instanceof ConnectionPoolTimeoutException )
                                                         {
                                                             logger.info( "ConnectionPoolTimeout start to retry times:"
                                                                                          + executionCount );
                                                             return true;
                                                         }
                                                         return false;
                                                     } );
        client = builder.build();
    }

    @PreDestroy
    public void destroy()
    {
        try
        {
            File downloadDir = new File( sidecarConfig.localRepository.get() );
            List<File> downloads = Files.walk( downloadDir.toPath() )
                                        .filter( Files::isRegularFile )
                                        .map( Path::toFile )
                                        .collect( Collectors.toList() );
            for ( File content : downloads )
            {
                content.delete();
            }
            downloadDir.delete();
        }
        catch ( IOException e )
        {
            logger.error( "Failed to clean up downloads", e );
        }
        finally
        {
            IOUtils.closeQuietly( client, null );
        }
    }

    public Optional<File> getLocally( final String path )
    {
        File download = new File( sidecarConfig.localRepository.orElse( DEFAULT_REPO_PATH ) + File.separator + path );
        if ( !download.exists() )
        {
            return Optional.empty();
        }
        return Optional.of( download );
    }

    public boolean shouldProxy( final String path )
    {
        return getBuildConfigId() == null || getBuildConfigId().trim().isEmpty() || path.endsWith( MAVEN_META )
                        || path.endsWith( NPM_META );
    }

    public String getBuildConfigId()
    {
        return System.getenv( BUILD_CONFIG_ID );
    }
}
