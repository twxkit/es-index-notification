package org.elasticsearch.contrib.plugin;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;

public class IndexRestAction extends BaseRestHandler {

    @Inject
    protected IndexRestAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(RestRequest.Method.POST, "/index-notification/{index}/{type}", this);

    }

    @Override
    public void handleRequest(RestRequest restRequest, RestChannel restChannel) {
        System.out.println("Here you go !!!!!!!!!");
    }
}
