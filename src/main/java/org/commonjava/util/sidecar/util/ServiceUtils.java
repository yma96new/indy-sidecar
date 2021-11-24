package org.commonjava.util.sidecar.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class ServiceUtils
{
    private final static Logger logger = LoggerFactory.getLogger( ServiceUtils.class );

    public static long parseTimeout( String timeout )
    {
        return Duration.parse( "pt" + timeout ).toMillis();
    }
}
