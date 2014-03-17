package org.elasticsearch.contrib.plugin;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;

public class IndexPlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "index-notification-plugin";
    }

    @Override
    public String description() {
        return "Notifies the indexer after index operation is complete";
    }

    public void onModule(RestModule module) {
        module.addRestAction(IndexRestAction.class);
    }
}
