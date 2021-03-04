package org.commonjava.util.sidecar.metrics;

public class MetricFieldsConstants {

    public static final String HTTP_METHOD = "http-method";

    public static final String STATUS_CODE = "status-code";

    public static final String PATH_INFO = "path-info";

    public static final String TRACE_ID = "trace-id";

    public static final String LATENCY_MILLIS = "latency_ms";

    public static final String ERROR_MESSAGE = "error_message";

    public static final String ERROR_CLASS = "error_class";

    public static final String SERVICE = "service";

    public static final String FUNCTION = "function";

    public static final String NOOP = "NOOP";

    public static String[] HEADERS = { HTTP_METHOD, STATUS_CODE, PATH_INFO, TRACE_ID, LATENCY_MILLIS, ERROR_MESSAGE, ERROR_CLASS };
}
