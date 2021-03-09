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
package org.commonjava.util.sidecar.metrics;

import io.vertx.core.http.HttpServerRequest;
import org.commonjava.o11yphant.honeycomb.HoneycombManager;
import org.commonjava.o11yphant.honeycomb.SimpleTraceSampler;
import org.commonjava.util.sidecar.config.SidecarHoneycombConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static org.commonjava.util.sidecar.metrics.MetricFieldsConstants.ERROR_CLASS;
import static org.commonjava.util.sidecar.metrics.MetricFieldsConstants.ERROR_MESSAGE;
import static org.commonjava.util.sidecar.metrics.MetricFieldsConstants.HTTP_METHOD;
import static org.commonjava.util.sidecar.metrics.MetricFieldsConstants.LATENCY_MILLIS;
import static org.commonjava.util.sidecar.metrics.MetricFieldsConstants.NOOP;
import static org.commonjava.util.sidecar.metrics.MetricFieldsConstants.PATH_INFO;
import static org.commonjava.util.sidecar.metrics.MetricFieldsConstants.TRACE_ID;
import static org.commonjava.util.sidecar.metrics.MetricFieldsConstants.STATUS_CODE;
import static org.commonjava.util.sidecar.services.ProxyService.HEADER_PROXY_TRACE_ID;


@ApplicationScoped
public class SidecarHoneycombManager
                extends HoneycombManager
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    public SidecarHoneycombManager( SidecarHoneycombConfiguration honeycombConfiguration )
    {
        super( honeycombConfiguration, new SimpleTraceSampler( honeycombConfiguration ) );
    }

    @PostConstruct
    public void init()
    {
        super.init();
//        registerRootSpanFields( GoldenSignalsRootSpanFields.getInstance() );
    }

    public void addFields( long elapse, HttpServerRequest request, Object item, Throwable err )
    {
        configuration.getFieldSet().forEach( field -> {
            Object value = getContext( elapse, field, request, item, err );
            if ( value != null )
            {
                logger.trace( "Add field, {}={}", field, value );
                addSpanField( field, value );
            }
        } );
    }

    private Object getContext( long elapse, String field, HttpServerRequest request, Object item, Throwable err )
    {
        Response resp = null;
        if ( item instanceof Response )
        {
            resp = (Response) item;
        }

        Object ret = null;
        switch ( field )
        {
            case HTTP_METHOD:
                ret = request.rawMethod();
                break;
            case STATUS_CODE:
                ret = ( resp != null ? resp.getStatus() : null );
                break;
            case PATH_INFO:
                ret = request.path();
                break;
            case TRACE_ID:
                String traceId = request.getHeader( HEADER_PROXY_TRACE_ID );
                ret = traceId == null ? NOOP : traceId;
                break;
            case LATENCY_MILLIS:
                ret = elapse;
                break;
            case ERROR_MESSAGE:
                ret = ( err != null ? err.getMessage() : null );
                break;
            case ERROR_CLASS:
                ret = ( err != null ? err.getClass().getName() : null );
                break;
            default:
                break;
        }
        return ret;
    }

}
