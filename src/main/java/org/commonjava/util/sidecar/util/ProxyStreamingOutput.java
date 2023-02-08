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
package org.commonjava.util.sidecar.util;

import io.opentelemetry.api.trace.Span;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.apache.commons.io.output.CountingOutputStream;
import org.commonjava.util.sidecar.model.TrackedContentEntry;
import org.commonjava.util.sidecar.services.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class ProxyStreamingOutput
                implements StreamingOutput
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private static final String MD5 = "MD5";

    private static final String SHA1 = "SHA-1";

    private static final String SHA256 = "SHA-256";

    private static final String[] DIGESTS = { MD5, SHA1, SHA256 };

    private static final long bufSize = 10 * 1024 * 1024;

    private final ResponseBody responseBody;

    private final TrackedContentEntry entry;

    private final String serviceOrigin;

    private final String indyOrigin;

    private final ReportService reportService;

    private final OtelAdapter otel;

    private final Map<String, MessageDigest> digests = new HashMap<>();

    public ProxyStreamingOutput( ResponseBody responseBody, TrackedContentEntry entry, String serviceOrigin,
                                 String indyOrigin, ReportService reportService, OtelAdapter otel )
    {
        this.responseBody = responseBody;
        this.entry = entry;
        this.serviceOrigin = serviceOrigin;
        this.indyOrigin = indyOrigin;
        this.reportService = reportService;
        this.otel = otel;

        for ( String key : DIGESTS )
        {
            try
            {
                digests.put( key, MessageDigest.getInstance( key ) );
            }
            catch ( NoSuchAlgorithmException e )
            {
                logger.warn( "Bytes hash calculation failed for request. Cannot get digest of type: {}", key );
            }
        }
    }

    @Override
    public void write( OutputStream output ) throws IOException
    {
        if ( responseBody != null )
        {
            try (CountingOutputStream cout = new CountingOutputStream( output ))
            {
                OutputStream out = cout;
                BufferedSource peek = responseBody.source().peek();
                while ( !peek.exhausted() )
                {
                    byte[] bytes;
                    if ( peek.request( bufSize ) )
                    {
                        bytes = peek.readByteArray(
                                        bufSize ); // byteCount bytes will be removed from current buffer after read
                    }
                    else
                    {
                        bytes = peek.readByteArray();
                    }
                    out.write( bytes );
                    if ( entry != null )
                    {
                        digests.values().forEach( d -> d.update( bytes ) );
                    }
                }
                out.flush();
                peek.close();
                if ( otel.enabled() )
                {
                    Span.current().setAttribute( "response.content_length", cout.getByteCount() );
                }
                if ( entry != null )
                {
                    entry.setSize( cout.getByteCount() );
                    String[] headers = indyOrigin.split( ":" );
                    entry.setOriginUrl(
                                    serviceOrigin + "/api/content/" + headers[0] + "/" + headers[1] + "/" + headers[2]
                                                    + entry.getPath() );
                    if ( digests.containsKey( MD5 ) )
                        entry.setMd5( DatatypeConverter.printHexBinary( digests.get( MD5 ).digest() ).toLowerCase() );

                    if ( digests.containsKey( SHA1 ) )
                        entry.setSha1( DatatypeConverter.printHexBinary( digests.get( SHA1 ).digest() ).toLowerCase() );

                    if ( digests.containsKey( SHA256 ) )
                        entry.setSha256( DatatypeConverter.printHexBinary( digests.get( SHA256 ).digest() )
                                                          .toLowerCase() );

                    reportService.appendDownload( entry );
                }
            }
            finally
            {
                if ( responseBody == null )
                {
                    return;
                }
                responseBody.close();
            }
        }
        else
        {
            if ( otel.enabled() )
            {
                Span.current().setAttribute( "response.content_length", 0 );
            }
        }
    }
}