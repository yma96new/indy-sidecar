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
package org.commonjava.util.sidecar.ftest;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.commonjava.util.sidecar.services.PreSeedConstants.DEFAULT_REPO_PATH;
import static org.commonjava.util.sidecar.util.TestUtil.SIZE_50K;
import static org.commonjava.util.sidecar.util.TestUtil.getBytes;

public class AbstractSidecarFuncTest
{
    private final String TRACKED_CONTENT = "{\n" + "\"buildConfigId\":\"9000\",\n" + "\"downloads\":\n" + "[{\n"
            + "    \"storeKey\" : \"maven:hosted:shared-imports\",\n"
            + "    \"path\" : \"/org/apache/maven/maven-core/3.0/maven-core-3.0.jar\",\n"
            + "    \"md5\" : \"9bd377874764a4fad7209021abfe7cf7\",\n"
            + "    \"sha256\" : \"ba03294ee53e7ba31838e4950f280d033c7744c6c7b31253afc75aa351fbd989\",\n"
            + "    \"sha1\" : \"73728ce32c9016c8bd05584301fa3ba3a6f5d20a\",\n" + "    \"size\" : 527040\n" + "  }\n"
            + "]}";

    private final String SUCCESS_BUILD = "9000";

    private final String PATH = "/org/apache/maven/maven-core/3.0/maven-core-3.0.jar";

    @BeforeEach
    public void prepare()
            throws IOException
    {
        deleteFiles();
        File tracked = new File( DEFAULT_REPO_PATH, SUCCESS_BUILD );
        FileUtils.write( tracked, new String( TRACKED_CONTENT.getBytes() ), "UTF-8" );

        File jar = new File( DEFAULT_REPO_PATH, PATH );
        FileUtils.write( jar, new String( getBytes( SIZE_50K ) ), "UTF-8" );
    }

    @AfterEach
    public void destroy()
    {
        deleteFiles();
    }

    private void deleteFiles()
    {
        List<File> files = new ArrayList<>();
        files.add( new File( DEFAULT_REPO_PATH, SUCCESS_BUILD ) );
        files.add( new File( DEFAULT_REPO_PATH, PATH ) );
        for ( File target : files )
        {
            if ( target.exists() )
            {
                target.delete();
            }
        }
    }

}
