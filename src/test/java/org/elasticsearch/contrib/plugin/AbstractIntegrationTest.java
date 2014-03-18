package org.elasticsearch.contrib.plugin;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.network.NetworkUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public abstract class AbstractIntegrationTest {
    private static final TimeValue TIMEOUT = TimeValue.timeValueSeconds(5);
    private final static ESLogger ES_LOGGER = Loggers.getLogger(AbstractIntegrationTest.class);
    protected static Node node;

    @BeforeClass
    public static void beforeAllTests() throws IOException {
        ES_LOGGER.info("In AbstractIntegrationTest.beforeAllTests");

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 9250;

        ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder()
                .put("path.data", "target/es-data")
                .put("path.plugins", projectRootDir() + "/plugins")
                .put("http.port", "9250")
                .put("cluster.name", "test-cluster-" + NetworkUtils.getLocalAddress())
                .put("gateway.type", "none")
                .put("action.auto_create_index", true);
        node = NodeBuilder.nodeBuilder().local(true).settings(settings).node();

    }

    private static String projectRootDir() throws IOException {
        String testClassPath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
        return new File(testClassPath).getParentFile().getParentFile().getCanonicalPath();
    }

    @AfterClass
    public static void afterAllTests() {

        ES_LOGGER.info("In AbstractNodeIntegrationTest.afterAllTests");

        // deleting all the indices before closing the node
        String[] indices = node.client().admin().cluster().prepareState()
                .execute().actionGet().getState().getMetaData()
                .concreteAllIndices();
        DeleteIndexResponse deleteIndexResponse = node.client().admin().indices()
                .prepareDelete(indices).execute().actionGet();
        ES_LOGGER.info("Delete indices [{}] acknowledged [{}]", indices.toString(), deleteIndexResponse.isAcknowledged());

        node.close();
    }

    protected CreateIndexResponse createIndexOnNode(String indexName) {
        if (indexExists(indexName)) {
            ES_LOGGER.warn("Index [" + indexName + "] already exists. Attempting to delete it.");
            DeleteIndexResponse deleteIndexResponse = deleteIndexOnNode(indexName);
            ES_LOGGER.info("Delete index [{}] acknowledged [{}]", indexName, deleteIndexResponse.isAcknowledged());
        }

        ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder();
        Settings settings = builder
                .put("index.mapper.dynamic", true)
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0)
                .build();

        CreateIndexResponse createIndexResponse = node.client().admin().indices().create(new CreateIndexRequest(indexName).settings(settings))
                .actionGet(TIMEOUT);
        waitForGreenClusterState(indexName);
        return createIndexResponse;

    }

    protected DeleteIndexResponse deleteIndexOnNode(String indexName) {
        if (!indexExists(indexName)) {
            ES_LOGGER.warn("Index [" + indexName + "] does not exist. Cannot delete.");
            return DeleteIndexAction.INSTANCE.newResponse();
        }

        return node.client().admin().indices().delete(new DeleteIndexRequest(indexName))
                .actionGet(TIMEOUT);
    }

    protected boolean indexExists(String name) {
        return node.client().admin().indices()
                .exists(new IndicesExistsRequest(name)).actionGet().isExists();
    }

    protected boolean typeExists(String indexName, String typeName) {
        return node.client().admin().indices()
                .prepareTypesExists(indexName).setTypes(typeName).execute().actionGet().isExists();
    }

    protected Map<Object, Object> indexSettings(String index) {
        JsonPath indexSettings = settings(index);
        return indexSettings.getMap(index + ".settings");
    }

    private JsonPath settings(String name) {
        return RestAssured.get("/" + name + "/_settings").body().jsonPath();
    }

    protected void refreshIndices(String... indices) {
        node.client().admin().indices().prepareRefresh(indices).execute().actionGet();
    }

    private ClusterHealthResponse waitForGreenClusterState(String index) {
        ClusterAdminClient clusterAdminClient = node.client().admin().cluster();
        ClusterHealthRequest request = new ClusterHealthRequestBuilder(clusterAdminClient).setIndices(index).setWaitForGreenStatus().request();
        return clusterAdminClient.health(request).actionGet();
    }


    protected CreateIndexResponse createIndexOnNode(String indexName, Settings settings) {
        if (indexExists(indexName)) {
            ES_LOGGER.warn("Index [" + indexName + "] already exists. Attempting to delete it.");
            DeleteIndexResponse deleteIndexResponse = deleteIndexOnNode(indexName);
            ES_LOGGER.info("Delete index [{}] acknowledged [{}]", indexName, deleteIndexResponse.isAcknowledged());
        }

        CreateIndexResponse createIndexResponse = node.client().admin().indices().create(new CreateIndexRequest(indexName).settings(settings))
                .actionGet(TIMEOUT);
        waitForGreenClusterState(indexName);
        return createIndexResponse;
    }
}
