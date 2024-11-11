package com.org.motadata.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;

import java.util.Map;

public class ConfigHelperUtil
{
    private static final LoggerUtil LOGGER = new LoggerUtil(ConfigHelperUtil.class);

    @JsonProperty("publicKeyPath")
    private String publicKeyPath = "/home/harsh/AIOPS_Training/public-key.pem";

    @JsonProperty("privateKeyPath")
    private String privateKeyPath = "/home/harsh/AIOPS_Training/private-key.pem";

    @JsonProperty("username")
    private String username = "admin";

    @JsonProperty("password")
    private String password = "Ck8ADQ5qyMRguRLYb/baqQ==";

    @JsonProperty("sslKeyStorePath")
    private String sslKeyStorePath = "/home/harsh/AIOPS_Training/keystore.jks";

    @JsonProperty("sslKeyStorePassword")
    private String sslKeyStorePassword = "r1LNQe0KrM//z0pVLq0UCw==";

    @JsonProperty("metricPollTime")
    private int metricPollTime = 12000;

    @JsonProperty("availibilityPollTime")
    private int availibilityPollTime = 6000;

    @JsonProperty("httpServerPort")
    private int httpServerPort = 8443;

    @JsonProperty("db")
    private DBConfig db = new DBConfig();

    @JsonProperty("workers")
    private WorkersConfig workers = new WorkersConfig();

    @JsonProperty("batchSizes")
    private BatchSizesConfig batchSizes = new BatchSizesConfig();

    public static class DBConfig {
        @JsonProperty("host")
        private String host = "localhost";

        @JsonProperty("port")
        private int port = 5432;

        @JsonProperty("database")
        private String database = "postgres";

        @JsonProperty("username")
        private String username = "harsh";

        @JsonProperty("password")
        private String password = "r1LNQe0KrM//z0pVLq0UCw==";

        @JsonProperty("maxConnections")
        private int maxConnections = 5;
    }

    public static class WorkersConfig {
        @JsonProperty("dbWorker")
        private int dbWorker = 4;

        @JsonProperty("queryBuilderWorker")
        private int queryBuilderWorker = 2;

        @JsonProperty("discoveryWorker")
        private int discoveryWorker = 2;

        @JsonProperty("pollingRouterWorker")
        private int pollingRouterWorker = 2;

        @JsonProperty("metricPollingInstances")
        private int metricPollingInstances = 2;

        @JsonProperty("availibilityPollingWorker")
        private int availibilityPollingWorker = 2;

        @JsonProperty("metricPollingWorker")
        private int metricPollingWorker = 2;
    }

    public static class BatchSizesConfig
    {
        @JsonProperty("metricPollingBatchSize")
        private int metricPollingBatchSize = 1;

        @JsonProperty("availibilityPollingBatchSize")
        private int availibilityPollingBatchSize = 1;
    }

    public static JsonObject getConfigJson(String jsonString)
    {
        try
        {
            var mapper = new ObjectMapper();

            return new JsonObject(mapper.convertValue(mapper.readValue(jsonString, ConfigHelperUtil.class), Map.class));
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }

        return null;
    }

}
