package org.elasticsearch.contrib.plugin;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.contrib.plugin.helper.*;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;

import static com.jayway.restassured.RestAssured.with;
import static name.mlnkrishnan.shouldJ.ShouldJ.it;

public class IndexDocumentIntegrationTest extends AbstractIntegrationTest {
    private String index = "ind1";
    private String type = "typ1";

    @After
    public void after() {
        deleteIndexOnNode(index);
    }

    @Test
    public void shouldIndexDocumentAndCallbackAfterIndexingComplete() throws Exception {
        Response response = with().content(Streams.copyToStringFromClasspath("/indexDocumentIntegrationTest-ValidDoc.json"))
                .with().headers("callback-url", "http://localhost:9797")
                .post("/index-notification/" + index + "/" + type).andReturn();

        it(response.statusCode()).shouldBe(200);
        JsonPath jsonPath = response.body().jsonPath();
        it(jsonPath.get("status")).shouldBe("SUCCESS");
        final String correlationId = jsonPath.get("correlation-id");
        it(correlationId).shouldNotBeNull();

        final RequestCaptureHandler handler = AllHandlers.requestCaptureHandler();

        new SimpleHttpServer(9797).registerHandler(handler).runWith(new Task() {
            @Override
            public Object execute() {
                Boolean taskResponse = new RetryTask(5, 3000).execute(new Task<Boolean>() {
                    @Override
                    public Boolean execute() {
                        String requestBody = handler.requestBody();
                        if (StringUtils.isEmpty(requestBody)) return null;

                        it(requestBody).shouldContain(index);
                        it(requestBody).shouldContain(type);
                        it(requestBody).shouldContain(correlationId);
                        it(handler.contentType()).shouldContain("application/json");

                        return true;
                    }
                });
                it(taskResponse).shouldBeTrue();
                return null;
            }
        });

        JsonPath searchResponse = search(index, type, "katta").getBody().jsonPath();
        it(searchResponse.get("hits.total")).shouldBe(1);
    }

    @Test
    public void shouldIndexDocument_EvenIfCallbackDoesNotExist() throws IOException, InterruptedException {
        Response response = with().content(Streams.copyToStringFromClasspath("/indexDocumentIntegrationTest-ValidDoc.json"))
                .post("/index-notification/" + index + "/" + type).andReturn();
        it(response.statusCode()).shouldBe(200);

        Thread.sleep(2 * 1000);
        refreshIndices(index);

        JsonPath searchResponse = search(index, type, "katta").getBody().jsonPath();
        it(searchResponse.get("hits.total")).shouldBe(1);
    }

    @Test
    public void shouldIndexDocumentWithSuppliedId() throws IOException, InterruptedException {
        String id = "randomId";
        Response response = with().content(Streams.copyToStringFromClasspath("/indexDocumentIntegrationTest-ValidDoc.json"))
                .put("/index-notification/" + index + "/" + type + "/" + id).andReturn();
        it(response.statusCode()).shouldBe(200);

        Thread.sleep(2 * 1000);
        refreshIndices(index);

        JsonPath searchResponse = search(index, type, "katta").getBody().jsonPath();
        it(searchResponse.get("hits.total")).shouldBe(1);
        it(searchResponse.get("hits.hits[0]._id")).shouldBe(id);
    }

    @Test
    public void shouldReIndexDocument() throws IOException, InterruptedException {
        String id = "randomId";

        //index initial document
        Response response = with().content(Streams.copyToStringFromClasspath("/indexDocumentIntegrationTest-ValidDoc.json"))
                .put("/index-notification/" + index + "/" + type + "/" + id).andReturn();
        it(response.statusCode()).shouldBe(200);

        Thread.sleep(2 * 1000);
        refreshIndices(index);

        JsonPath searchResponse = search(index, type, "katta").getBody().jsonPath();
        it(searchResponse.get("hits.total")).shouldBe(1);
        it(searchResponse.get("hits.hits[0]._id")).shouldBe(id);

        //re-index the same document with different content
        response = with().content(Streams.copyToStringFromClasspath("/indexDocumentIntegrationTest-ValidDocWithRole.json"))
                .put("/index-notification/" + index + "/" + type + "/" + id).andReturn();
        it(response.statusCode()).shouldBe(200);

        Thread.sleep(2 * 1000);
        refreshIndices(index);

        searchResponse = search(index, type, "dev").getBody().jsonPath();
        it(searchResponse.get("hits.total")).shouldBe(1);
        it(searchResponse.get("hits.hits[0]._id")).shouldBe(id);
    }
}



