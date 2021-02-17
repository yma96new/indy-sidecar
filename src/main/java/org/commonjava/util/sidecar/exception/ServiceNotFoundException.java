package org.commonjava.util.sidecar.exception;

public class ServiceNotFoundException
        extends Exception
{
    public ServiceNotFoundException( String message )
    {
        super( message );
    }
}