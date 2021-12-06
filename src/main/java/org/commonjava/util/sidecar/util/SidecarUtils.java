package org.commonjava.util.sidecar.util;

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

    @FunctionalInterface
    public interface CheckedFunction<T, R>
    {
        R apply( T t ) throws Exception;
    }
}