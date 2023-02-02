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
package org.commonjava.util.sidecar.ftest.profile;

import io.quarkus.test.junit.QuarkusTestProfile;
import org.commonjava.util.sidecar.ftest.mock.BuildConfigIdEnvMock;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.commonjava.util.sidecar.services.PreSeedConstants.DEFAULT_REPO_PATH;

public class SidecarFunctionProfile
                implements QuarkusTestProfile
{

    @Override
    public Map<String, String> getConfigOverrides()
    {
        Map<String, String> configs = new HashMap<>();
        configs.put( "sidecar.local-repository", DEFAULT_REPO_PATH );
        return configs;
    }

    @Override
    public String getConfigProfile()
    {
        return "dev";
    }

    @Override
    public Set<Class<?>> getEnabledAlternatives()
    {
        return Stream.of( BuildConfigIdEnvMock.class ).collect( Collectors.toSet() );
    }
}
