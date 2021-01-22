package org.commonjava.util.sidecar;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class HealthResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/healthcheck")
          .then()
             .statusCode(200)
             .body(is("Pong"));
    }

}