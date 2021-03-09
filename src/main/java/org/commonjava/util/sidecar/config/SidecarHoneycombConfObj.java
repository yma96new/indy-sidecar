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
package org.commonjava.util.sidecar.config;

import io.quarkus.arc.config.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@ConfigProperties( prefix = "honeycomb" )
public class SidecarHoneycombConfObj
{
    public Optional<Boolean> enabled;

    @ConfigProperty( name = "console-transport" )
    public Optional<Boolean> consoleTransport;

    public Optional<String> dataset;

    @ConfigProperty( name = "write-key" )
    public Optional<String> writeKey;

    @ConfigProperty( name = "base-sample-rate" )
    public Optional<Integer> baseSampleRate;

    public Optional<String> functions;
}
