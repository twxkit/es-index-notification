package org.elasticsearch.contrib.plugin;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.contrib.plugin.helper.*;
import org.junit.After;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.with;
import static name.mlnkrishnan.shouldJ.ShouldJ.it;

public class IndexDocumentIntegrationTest extends AbstractIntegrationTest {
    private String index = "ind1";
    private String type = "typ1";

    @After
    public void after() {
        deleteIndexOnNode("ind1");
    }

    @Test
    public void shouldIndexDocumentAndCallbackAfterIndexingComplete() throws Exception {
        Response response = with().content(Streams.copyToStringFromClasspath("/indexDocumentIntegrationTest-ValidDoc.json"))
                .with().headers("callback-url", "http://localhost:9999")
                .post("/index-notification/" + index + "/" + type).andReturn();

        it(response.statusCode()).shouldBe(200);
        JsonPath jsonPath = response.body().jsonPath();
        it(jsonPath.get("status")).shouldBe("success");

        final RequestCaptureHandler handler = AllHandlers.requestCaptureHandler();

        new SimpleHttpServer(9999).registerHandler(handler).runWith(new Task() {
            @Override
            public Object execute() {
                Boolean taskResponse = new RetryTask(5, 3000).execute(new Task<Boolean>() {
                    @Override
                    public Boolean execute() {
                        String requestBody = handler.requestBody();
                        if (StringUtils.isEmpty(requestBody)) return null;

                        it(requestBody).shouldContain(index);
                        it(requestBody).shouldContain(type);
                        it(handler.contentType()).shouldContain("application/json");

                        return true;
                    }
                });
                it(taskResponse).shouldBeTrue();
                return null;
            }
        });

    }
}



