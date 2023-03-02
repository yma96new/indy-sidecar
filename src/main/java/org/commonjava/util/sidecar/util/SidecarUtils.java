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

import okhttp3.MediaType;

import java.time.Duration;

import static io.vertx.core.http.impl.HttpUtils.normalizePath;

public class SidecarUtils
{
    public final static String BUILD_CONFIG_ID = "build.config.id";

    public static String getBuildConfigId()
    {
        return System.getenv( BUILD_CONFIG_ID );
    }

    public static long parseTimeout( String timeout )
    {
        return Duration.parse( "pt" + timeout ).toMillis();
    }

    public static <R> R normalizePathAnd( String path, CheckedFunction<String, R> action ) throws Exception
    {
        return action.apply( normalizePath( path ) );
    }

    public static MediaType getMediaType( String contentType )
    {
        if ( contentType != null )
        {
            return MediaType.get( contentType );
        }
        return null;
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R>
    {
        R apply( T t ) throws Exception;
    }
}