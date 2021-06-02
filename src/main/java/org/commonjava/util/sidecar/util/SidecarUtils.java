package org.commonjava.util.sidecar.util;

public class SidecarUtils
{
    public final static String BUILD_CONFIG_ID = "build.config.id";

    public static String getBuildConfigId()
    {
        return System.getenv( BUILD_CONFIG_ID );
    }
}