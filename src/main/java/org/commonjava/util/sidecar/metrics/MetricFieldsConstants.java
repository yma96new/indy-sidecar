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

public class MetricFieldsConstants
{

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

    public static String[] HEADERS =
                    { HTTP_METHOD, STATUS_CODE, PATH_INFO, TRACE_ID, LATENCY_MILLIS, ERROR_MESSAGE, ERROR_CLASS };
}
