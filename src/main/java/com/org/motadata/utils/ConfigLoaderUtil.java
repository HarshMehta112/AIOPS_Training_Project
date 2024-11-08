package com.org.motadata.utils;

import com.org.motadata.Bootstrap;
import com.org.motadata.constant.Constants;
import com.org.motadata.database.DatabaseService;
import com.org.motadata.flyway.FlywayExecutor;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/30/24 1:08 PM
 */
public class ConfigLoaderUtil
{
    private static final LoggerUtil LOGGER = new LoggerUtil(ConfigLoaderUtil.class);

    private static JWTAuth jwtAuth;
    private static String loginUsername;

    private static String loginPassword;

    private static DatabaseService databaseServiceProxy;

    private static long availibilityPollTime;

    private static long metricPollTime;

    private static int dbWorker;

    private static int queryBuilderWorker;

    private static int discoveryWorker;

    private static int pollingRouterWorker;

    private static int availibilityPollingWorker;

    private static int metricPollingWorker;

    private static int availibilityPollingBatchSize;

    private static int metricPollingBatchSize;

    private static int metricPollingInstances;

    public static int getMetricPollingInstances() {
        return metricPollingInstances;
    }

    public static void setMetricPollingInstances(int metricPollingInstances) {
        ConfigLoaderUtil.metricPollingInstances = metricPollingInstances;
    }

    public static int getDbWorker() {
        return dbWorker;
    }

    public static void setDbWorker(int dbWorker) {
        ConfigLoaderUtil.dbWorker = dbWorker;
    }

    public static int getQueryBuilderWorker() {
        return queryBuilderWorker;
    }

    public static void setQueryBuilderWorker(int queryBuilderWorker) {
        ConfigLoaderUtil.queryBuilderWorker = queryBuilderWorker;
    }

    public static int getDiscoveryWorker() {
        return discoveryWorker;
    }

    public static void setDiscoveryWorker(int discoveryWorker) {
        ConfigLoaderUtil.discoveryWorker = discoveryWorker;
    }

    public static int getPollingRouterWorker() {
        return pollingRouterWorker;
    }

    public static void setPollingRouterWorker(int pollingRouterWorker) {
        ConfigLoaderUtil.pollingRouterWorker = pollingRouterWorker;
    }

    public static int getAvailibilityPollingWorker() {
        return availibilityPollingWorker;
    }

    public static void setAvailibilityPollingWorker(int availibilityPollingWorker) {
        ConfigLoaderUtil.availibilityPollingWorker = availibilityPollingWorker;
    }

    public static int getMetricPollingWorker() {
        return metricPollingWorker;
    }

    public static void setMetricPollingWorker(int metricPollingWorker) {
        ConfigLoaderUtil.metricPollingWorker = metricPollingWorker;
    }

    public static int getAvailibilityPollingBatchSize() {
        return availibilityPollingBatchSize;
    }

    public static void setAvailibilityPollingBatchSize(int availibilityPollingBatchSize) {
        ConfigLoaderUtil.availibilityPollingBatchSize = availibilityPollingBatchSize;
    }

    public static int getMetricPollingBatchSize() {
        return metricPollingBatchSize;
    }

    public static void setMetricPollingBatchSize(int metricPollingBatchSize) {
        ConfigLoaderUtil.metricPollingBatchSize = metricPollingBatchSize;
    }

    public static long getAvailibilityPollTime() {
        return availibilityPollTime;
    }

    public static void setAvailibilityPollTime(long availibilityPollTime) {
        ConfigLoaderUtil.availibilityPollTime = availibilityPollTime;
    }

    public static long getMetricPollTime() {
        return metricPollTime;
    }

    public static void setMetricPollTime(long metricPollTime) {
        ConfigLoaderUtil.metricPollTime = metricPollTime;
    }

    public static DatabaseService getDatabaseServiceProxy() {
        return databaseServiceProxy;
    }

    public static void setDatabaseServiceProxy(DatabaseService databaseServiceProxy) {
        ConfigLoaderUtil.databaseServiceProxy = databaseServiceProxy;
    }

    public static String getSslKeystorePath() {
        return sslKeystorePath;
    }

    public static void setSslKeystorePath(String sslKeystorePath) {
        ConfigLoaderUtil.sslKeystorePath = sslKeystorePath;
    }

    public static String getSslKeystorePassword() {
        return sslKeystorePassword;
    }

    public static void setSslKeystorePassword(String sslKeystorePassword) {
        ConfigLoaderUtil.sslKeystorePassword = sslKeystorePassword;
    }

    private static String sslKeystorePath;

    private static String sslKeystorePassword;

    private static String dbHost;

    private static String dbName;

    private static final int workerPoolHelper = 16;

    private static final int eventLoopWorker = 8;

    public static int getWorkerPoolHelper() {
        return workerPoolHelper;
    }

    public static int getEventLoopWorker() {
        return eventLoopWorker;
    }

    public static String getDbHost() {
        return dbHost;
    }

    public static void setDbHost(String dbHost) {
        ConfigLoaderUtil.dbHost = dbHost;
    }

    public static String getDbName() {
        return dbName;
    }

    public static void setDbName(String dbName) {
        ConfigLoaderUtil.dbName = dbName;
    }

    public static String getDbUsername() {
        return dbUsername;
    }

    public static void setDbUsername(String dbUsername) {
        ConfigLoaderUtil.dbUsername = dbUsername;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    public static void setDbPassword(String dbPassword) {
        ConfigLoaderUtil.dbPassword = dbPassword;
    }

    public static int getDbPort() {
        return dbPort;
    }

    public static void setDbPort(int dbPort) {
        ConfigLoaderUtil.dbPort = dbPort;
    }

    private static String dbUsername;

    private static String dbPassword;

    private static int dbPort;

    private static int dbMaxConnections;

    private static int httpServerPort;

    public static int getHttpServerPort() {
        return httpServerPort;
    }

    public static void setHttpServerPort(int httpServerPort) {
        ConfigLoaderUtil.httpServerPort = httpServerPort;
    }

    public static String getLoginUsername() {
        return loginUsername;
    }

    public static int getDbMaxConnections() {
        return dbMaxConnections;
    }

    public static void setDbMaxConnections(int dbMaxConnections) {
        ConfigLoaderUtil.dbMaxConnections = dbMaxConnections;
    }

    public static void setLoginUsername(String loginUsername) {
        ConfigLoaderUtil.loginUsername = loginUsername;
    }

    public static String getLoginPassword() {
        return loginPassword;
    }

    public static void setLoginPassword(String loginPassword) {
        ConfigLoaderUtil.loginPassword = loginPassword;
    }

    public static JWTAuth getJwtAuth() {
        return jwtAuth;
    }

    public static void setJwtAuth(JWTAuth jwtAuth) {
        ConfigLoaderUtil.jwtAuth = jwtAuth;
    }


    public static Future<Boolean> init()
    {
        Promise<Boolean> promise = Promise.promise();

        // Load properties file
        var properties = new Properties();

        try (FileInputStream inputStream = new FileInputStream(CommonUtil.buildString
                (Constants.RESOURCES_PATH,Constants.PATH_SEPARATOR,Constants.PROPERTIES_FILE)))
        {

            properties.load(inputStream);

            setLoginUsername(properties.getProperty(Constants.USER_NAME));

            setLoginPassword(properties.getProperty(Constants.PASSWORD));

            setSslKeystorePath(properties.getProperty(Constants.SSL_KEYSTORE_PATH));

            setSslKeystorePassword(properties.getProperty(Constants.SSL_KEYSTORE_PASSWORD));

            setHttpServerPort(Integer.parseInt(properties.getProperty(Constants.HTTP_PORT)));

            setUpJWTAuth(properties);

            DatabaseService databaseServiceProxy = DatabaseService.createProxy(Bootstrap.getVertx(),
                    "database.service.address");

            setDatabaseServiceProxy(databaseServiceProxy);

            setAvailibilityPollTime(Long.parseLong(properties.
                    getProperty(Constants.AVAILIBILITY_POLLING_TIME)));

            setMetricPollTime(Long.parseLong(properties.
                    getProperty(Constants.METRIC_POLLING_TIME)));

            setDbHost(properties.getProperty(Constants.DB_HOST));

            setDbName(properties.getProperty(Constants.DB_DATABASE_NAME));

            setDbPassword(properties.getProperty(Constants.DB_PASSWORD));

            setDbPort(Integer.parseInt(properties.getProperty(Constants.DB_PORT)));

            setDbUsername(properties.getProperty(Constants.DB_USERNAME));

            setDbMaxConnections(Integer.parseInt(properties.getProperty(Constants.DB_MAX_CONNECTIONS)));

            setDbWorker(Integer.parseInt(properties.getProperty(Constants.DB_WORKERS)));

            setDiscoveryWorker(Integer.parseInt(properties.getProperty(Constants.DISCOVERY_WORKER)));

            setAvailibilityPollingWorker(Integer.parseInt(properties.getProperty(Constants.AVAILIBILITY_POLLING_WORKER)));

            setMetricPollingWorker(Integer.parseInt(properties.getProperty(Constants.METRIC_POLLING_WORKER)));

            setQueryBuilderWorker(Integer.parseInt(properties.getProperty(Constants.QUERY_BUILDER_WORKER)));

            setPollingRouterWorker(Integer.parseInt(properties.getProperty(Constants.POLLING_ROUTER_WORKER)));

            setMetricPollingBatchSize(Integer.parseInt(properties.getProperty(Constants.METRIC_POLLING_BATCH)));

            setAvailibilityPollingBatchSize(Integer.parseInt(properties.getProperty(Constants.AVAILIBILITY_POLLING_BATCH)));

            setMetricPollingInstances(Integer.parseInt(properties.getProperty(Constants.METRIC_POLLING_INSTANCES)));

            FlywayExecutor.executeDbMigration().onComplete(migrator->
            {
                if(migrator.succeeded())
                {
                    LOGGER.info("configurations loaded and setting up of configurations completed..");

                    promise.complete(true);
                }
                else
                {
                    LOGGER.error("Some issue occurred in db migration "+
                            migrator.cause(), migrator.cause().getStackTrace());

                    promise.fail(migrator.cause());
                }
            });

        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());

            LOGGER.warn("Failed to load config.properties");

            promise.fail(exception);
        }

        return promise.future();
    }

    private static void setUpJWTAuth(Properties properties)
    {
        // Get paths for public and private keys
        var publicKeyPath = properties.getProperty("publicKeyPath");

        var privateKeyPath = properties.getProperty("privateKeyPath");

        if (!(CommonUtil.isNonNull.test(privateKeyPath)
                || CommonUtil.isNonNull.test(publicKeyPath)))
        {
            LOGGER.warn("Key paths are not set in config.properties");

            throw new NullPointerException();
        }

        // Load the public and private key buffers
        var publicKeyBuffer = Bootstrap.getVertx().fileSystem().readFileBlocking(publicKeyPath);

        var privateKeyBuffer = Bootstrap.getVertx().fileSystem().readFileBlocking(privateKeyPath);

        // Configure JWT authentication with public and private keys
        jwtAuth = JWTAuth.create(Bootstrap.getVertx(), new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm(Constants.JWT_TOKEN_ALGORITHM).setBuffer(publicKeyBuffer))
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm(Constants.JWT_TOKEN_ALGORITHM).setBuffer(privateKeyBuffer))
        );

        setJwtAuth(jwtAuth);
    }



}
