package org.elasticsearch.contrib.plugin;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.elasticsearch.common.io.Streams;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.with;
import static name.mlnkrishnan.shouldJ.ShouldJ.it;

public class IndexDocumentIntegrationTest extends AbstractIntegrationTest {

    @After
    public void after() {
        deleteIndexOnNode("ind1");
    }

    @Test
    public void shouldIndex() throws IOException {
        Response response = with().content(Streams.copyToStringFromClasspath("/indexDocumentIntegrationTest-ValidDoc.json"))
                .post("/index-notification/ind1/typ1").andReturn();

        it(response.statusCode()).shouldBe(200);

        JsonPath jsonPath = response.body().jsonPath();
        it(jsonPath.getBoolean("ok")).shouldBeTrue();

    }
}



