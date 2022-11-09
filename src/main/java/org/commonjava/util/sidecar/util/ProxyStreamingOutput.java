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
package org.commonjava.util.sidecar.util;

import io.opentelemetry.api.trace.Span;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProxyStreamingOutput
                implements StreamingOutput
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final InputStream bodyStream;

    private final OtelAdapter otel;

    public ProxyStreamingOutput( InputStream bodyStream, OtelAdapter otel )
    {
        this.bodyStream = bodyStream;
        this.otel = otel;
    }

    @Override
    public void write( OutputStream output ) throws IOException
    {
        if ( bodyStream != null )
        {
            try
            {
                OutputStream out = output;
                CountingOutputStream cout = new CountingOutputStream( out );
                out = cout;
                logger.trace( "Copying from: {} to: {}", bodyStream, out );
                IOUtils.copy( bodyStream, out );

                if ( otel.enabled() )
                {
                    Span.current().setAttribute( "response.content_length", cout.getByteCount() );
                }
            }
            finally
            {
                closeBodyStream( bodyStream );
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

    private void closeBodyStream( InputStream is )
    {
        if ( is == null )
        {
            return;
        }

        try
        {
            is.close();
        }
        catch ( IOException e )
        {
            if ( otel.enabled() )
            {
                Span.current().setAttribute( "body.ignored_error_class", e.getClass().getSimpleName() );
                Span.current().setAttribute( "body.ignored_error_class", e.getMessage() );
            }
            logger.trace( "Failed to close body stream in proxy response.", e );
        }
    }
}