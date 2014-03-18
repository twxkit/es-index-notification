package org.elasticsearch.contrib.plugin;

import com.jayway.restassured.http.ContentType;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor;
import org.elasticsearch.rest.*;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.jayway.restassured.RestAssured.with;

public class IndexRestAction extends BaseRestHandler {

    private final static ESLogger ES_LOGGER = Loggers.getLogger(IndexRestAction.class);


    private final EsThreadPoolExecutor executorService;

    @Inject
    protected IndexRestAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(RestRequest.Method.POST, "/index-notification/{index}/{type}", this);

        this.executorService = EsExecutors.newScalingExecutorService(1, 10, 1, TimeUnit.MINUTES, EsExecutors.daemonThreadFactory("es-index-notification"));
    }

    @Override
    public void handleRequest(final RestRequest restRequest, RestChannel restChannel) {
        final String index = restRequest.param("index");
        final String type = restRequest.param("type");

        final String callbackUrl = restRequest.header("callback-url");

        this.executorService.submit(new Callable<IndexResponse>() {
            @Override
            public IndexResponse call() throws Exception {
                IndexResponse indexResponse = client.prepareIndex(index, type).setSource(restRequest.content()).execute().actionGet();

                com.jayway.restassured.response.Response callbackResponse =
                        with().content(Serializer.toJson(indexResponse)).contentType(ContentType.JSON).post(callbackUrl).andReturn();

                ES_LOGGER.info("Callback response from URL {} : {}", callbackUrl, callbackResponse.body().asString());

                return indexResponse;
            }
        });

        restChannel.sendResponse(new StringRestResponse(RestStatus.OK, Response.successfulResponse().toJson()));

    }
}
