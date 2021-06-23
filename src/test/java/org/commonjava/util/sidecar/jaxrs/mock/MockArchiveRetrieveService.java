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
package org.commonjava.util.sidecar.jaxrs.mock;

import org.apache.commons.io.FileUtils;
import org.commonjava.util.sidecar.services.ArchiveRetrieveService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.io.File;
import java.io.IOException;

import static org.commonjava.util.sidecar.services.PreSeedConstants.DEFAULT_REPO_PATH;
import static org.commonjava.util.sidecar.util.TestUtil.SIZE_50K;
import static org.commonjava.util.sidecar.util.TestUtil.getBytes;

@ApplicationScoped
@Alternative
public class MockArchiveRetrieveService
                extends ArchiveRetrieveService
{
    @Override
    public void init()
    {
        super.init();
        try
        {
            File tracked = new File( DEFAULT_REPO_PATH, "1000" );
            FileUtils.write( tracked, new String( getBytes( SIZE_50K ) ), "UTF-8" );

            String path = "/org/apache/maven/maven-core/3.0/maven-core-3.0.jar";
            File jar = new File( DEFAULT_REPO_PATH + path );
            new File( jar.getParent() ).mkdirs();
            FileUtils.write( jar, new String( getBytes( SIZE_50K ) ), "UTF-8" );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public String getBuildConfigId()
    {
        return "1000";
    }
}
