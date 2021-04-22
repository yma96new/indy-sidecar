package org.commonjava.util.sidecar.util;

public class SidecarUtils
{
    public final static String BUILD_CONFIG_ID = "build.config.id";

    private final static String MAVEN_META = "maven-metadata.xml";

    public static boolean shouldProxy( final String path )
    {
        return getBuildConfigId() == null || getBuildConfigId().trim().isEmpty() || path.endsWith( MAVEN_META );
    }

    public static String getBuildConfigId()
    {
        return System.getProperty( BUILD_CONFIG_ID );
    }
}
