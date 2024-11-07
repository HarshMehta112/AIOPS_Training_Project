package com.org.motadata.constant;

import java.nio.file.FileSystems;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 3:39 PM
 */
public class Constants
{
    public static final String USER_NAME = "username";

    public static final String PASSWORD = "password";

    public static final String JWT_TOKEN_ALGORITHM = "RS512";

    public static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();

    public static final String RESOURCES_PATH = "/home/harsh/Project/AIOPS_Training_Project/src/main/resources/";

    public static final String PROPERTIES_FILE = "config.properties";

    public static final String VALUE_SEPARATOR = "_|@#|_";

    public static final String VALUE_SEPARATOR_WITH_ESCAPE = "_\\|@#\\|_";

    public static final int HTTP_UNAUTHORIZED_STATUS_CODE = 401;

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    public static final String DISCOVERY_NAME = "discovery_name";

    public static final String CREDENTIAL_PROFILE_NAME = "credential_profile_name";

    public static final String PORT = "port";

    public static final String CREDENTIAL_PROFILE_ID = "credential_profile_id";

    public static final String IP_ADDRESS = "ip_address";

    public static final String ACCESS_TOKEN = "accessToken";

    public static final String REFRESH_TOKEN = "refreshToken";

    public static final String DEBUG_LOG_FLAG = "debugEnabled";

    public static final String NEW_LINE_SEPARATOR = "\n";

    public static final String SSL_KEYSTORE_PATH = "sslKeyStorePath";

    public static final String SSL_KEYSTORE_PASSWORD = "sslKeyStorePassword";

    public static final String SSH_USERNAME = "ssh_username";

    public static final String SSH_PASSWORD = "ssh_password";

    public static final String DEVICE_TYPE = "device_type";

    public static final String SSH = "ssh";

    public static final String PING = "ping";

    public static final String MONITOR_ID = "monitor_id";

    public static final String QUERY_BUILD_REQUEST = "db.query.build.request";

    public static final String DISCOVERY_RUN_REQUEST = "discovery.run.request";

    public static final String INSERT_OPERATION = "INSERT";

    public static final String DELETE_OPERATION = "DELETE";

    public static final String UPDATE_OPERATION = "UPDATE";

    public static final String SELECT_OPERATION = "SELECT";

    public static final String BATCH_INSERT_OPERATION = "BATCH_INSERT";

    public static final String SP_CALL = "SP_CALL";

    public static final String DB_TABLE_NAME = "tableName";

    public static final String DB_OPERATION_TYPE = "operationType";

    public static final String DB_VALUES = "values";

    public static final String DB_CONDITIONS = "conditions";

    public static final String DB_REQUESTS = "database.operations";

    public static final String POLLING_REQUESTS = "polling.requests";

    public static final String AVAILIBILITY_POLLING_REQUESTS = "availibility.polling.requests";

    public static final String METRIC_POLLING_REQUESTS = "metric.polling.requests";

    public static final String QUERY = "query";

    public static final String CREDENTIAL_PROFILE_TABLE = "tbl_credentials";

    public static final String DISCOVERY_PROFILE_TABLE = "tbl_discoveries";

    public static final String MONITOR_TABLE = "tbl_monitor";

    public static final String ENCRYPTION_ALGORITHM = "AES";

    public static final String ENCRYPTION_KEY = "AIOPSTrainingProject@123";

    public static final String PLUGIN_CALL_CATEGORY = "category";

    public static final String DISCOVERY = "discovery";

    public static final String POLLING = "polling";

    public static final String ID = "id";

    public static final short ZERO = 0;

    public static final String DISCOVERY_ID = "discovery_id";

    public static final String DISCOVERED_FLAG = "discovered";

    public static final String STATUS = "status";

    public static final String DISCOVERY_PROVISION_SP = "CALL insert_into_monitor(###)";

    public static final String AVAILIBILITY_POLLING_TIME = "availibility_poll_time";

    public static final String METRIC_POLLING_TIME = "metric_poll_time";

    public static final String RUN_DISCOVERY_DATA_QUERY = "\n" +
            "SELECT d.id AS discovery_id, d.ip_address, d.port, c.ssh_username, c.ssh_password FROM tbl_discoveries d JOIN tbl_credentials c ON d.credential_profile_id = c.id WHERE d.id = "+ "###" +";";

    public static final String METRIC_POLLING_DATA_QUERY = "SELECT d.id, d.ip_address, d.port, c.ssh_username, c.ssh_password FROM tbl_monitor d JOIN tbl_credentials c ON d.credential_profile_id = c.id WHERE ###;";

    public static final String METRIC_INSERT_QUERY = "INSERT INTO tbl_metric (id, metric_name, metric_value) VALUES ($1, $2, $3)";

    public static final String DB_HOST = "dbHost";

    public static final String DB_PORT = "dbPort";

    public static final String DB_DATABASE_NAME = "dbDatabase";

    public static final String DB_USERNAME = "dbUser";

    public static final String DB_PASSWORD = "dbPassword";

    public static final String DB_MAX_CONNECTIONS = "dbMaxConnections";

    public static final String HTTP_PORT = "HttpServerPort";

    public static final String DB_SSL_CERT_PATH = "/etc/postgresql/ssl/postgresql.crt";

    public static final String DB_WORKERS = "dbWorker";

    public static final String QUERY_BUILDER_WORKER = "queryBuilderWorker";

    public static final String DISCOVERY_WORKER = "discoveryWorker";

    public static final String POLLING_ROUTER_WORKER = "pollingRouterWorker";

    public static final String AVAILIBILITY_POLLING_WORKER = "availibilityPollingWorker";

    public static final String METRIC_POLLING_WORKER = "metricPollingWorker";

    public static final String METRIC_POLLING_INSTANCES = "metricPollingInstances";

    public static final String AVAILIBILITY_POLLING_BATCH = "availibilityPollingBatchSize";

    public static final String METRIC_POLLING_BATCH = "metricPollingBatchSize";

    public static final String EVENTLOOP_POOL_SIZE = "eventloopPoolSize";

    public static final String WORKER_POOL_SIZE = "workerPoolSize";

    public static final String HASH_SEPARATOR = "###";
}