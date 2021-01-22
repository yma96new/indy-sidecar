package org.commonjava.util.sidecar;

import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativeHealthResourceIT extends HealthResourceTest {

    // Execute the same tests but in native mode.
}