package org.elasticsearch.contrib.plugin;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.*;
import org.json.simple.JSONObject;

public class IndexRestAction extends BaseRestHandler {

    @Inject
    protected IndexRestAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(RestRequest.Method.POST, "/index-notification/{index}/{type}", this);

    }

    @Override
    public void handleRequest(RestRequest restRequest, RestChannel restChannel) {
        String index = restRequest.param("index");
        String type = restRequest.param("type");

        String document = restRequest.content().toUtf8();

        IndexResponse indexResponse = client.prepareIndex(index, type).setSource(restRequest.content()).execute().actionGet();
        JSONObject response = Serializer.toJsonObject(indexResponse);
        response.put("ok", "true");

        restChannel.sendResponse(new StringRestResponse(RestStatus.OK, response.toJSONString()));
    }
}
