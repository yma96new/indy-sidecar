package org.commonjava.util.sidecar.util;

import java.time.Duration;

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
}