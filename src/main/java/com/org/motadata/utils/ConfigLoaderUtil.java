package com.org.motadata.utils;

import com.org.motadata.Bootstrap;
import com.org.motadata.constant.Constants;
import com.org.motadata.database.DatabaseService;
import com.org.motadata.flyway.FlywayExecutor;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/30/24 1:08 PM
 */
public class ConfigLoaderUtil
{
    private static final LoggerUtil LOGGER = new LoggerUtil(ConfigLoaderUtil.class);

    private static JWTAuth jwtAuth;

    private static JsonObject configs;

    private static DatabaseService databaseServiceProxy;

    public static JsonObject getConfigs() {
        return configs;
    }

    public static void setConfigs(JsonObject configs) {
        ConfigLoaderUtil.configs = configs;
    }

    public static DatabaseService getDatabaseServiceProxy() {
        return databaseServiceProxy;
    }

    public static void setDatabaseServiceProxy(DatabaseService databaseServiceProxy) {
        ConfigLoaderUtil.databaseServiceProxy = databaseServiceProxy;
    }

    public static JWTAuth getJwtAuth() {
        return jwtAuth;
    }

    public static void setJwtAuth(JWTAuth jwtAuth) {
        ConfigLoaderUtil.jwtAuth = jwtAuth;
    }


    public static Future<Boolean> init()
    {
        var promise = Promise.<Boolean>promise();

        try
        {
            Objects.requireNonNull(loadConfig()).onComplete(configHandler->
            {
                if(configHandler.succeeded())
                {
                    var configs = configHandler.result();

                    if(CommonUtil.isNonNull.test(configs))
                    {
                        setConfigs(ConfigHelperUtil.getConfigJson(configs.toString()));

                        LOGGER.info(getConfigs().encodePrettily());

                        setUpJWTAuth(configs);

                        DatabaseService databaseServiceProxy = DatabaseService.createProxy(Bootstrap.getVertx(),
                                "database.service.address");

                        setDatabaseServiceProxy(databaseServiceProxy);

                        FlywayExecutor.executeDbMigration().onComplete(migrator->
                        {
                            if(migrator.succeeded())
                            {
                                LOGGER.info("configurations loaded and setting up of configurations completed..");
                            }
                            else
                            {
                                LOGGER.error("Some issue occurred in db migration "+
                                        migrator.cause(), migrator.cause().getStackTrace());
                            }
                        });

                        promise.complete(true);
                    }
                }
            });
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());

            promise.fail(exception);
        }

        return promise.future();
    }

    private static void setUpJWTAuth(JsonObject properties)
    {
        // Get paths for public and private keys
        var publicKeyPath = properties.getString("publicKeyPath");

        var privateKeyPath = properties.getString("privateKeyPath");

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

/*    public static Future<JsonObject> loadConfig(String configFilePath)
    {
        var fileSystem = Bootstrap.getVertx().fileSystem();

        // Read the file asynchronously
        return fileSystem.readFile(configFilePath)
                .map(file -> new JsonObject(file.toString()))
                .onFailure(error -> LOGGER.error("Error reading configuration file: " +
                        error.getMessage(), error.getStackTrace()));
    }*/

    public static Future<JsonObject> loadConfig()
    {
        var promise = Promise.<JsonObject>promise();

        var inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(Constants.CONFIG_FILE);

        if (inputStream == null)
        {
            LOGGER.warn("config.json file not found in resources");

            promise.fail("InputStream is null");

            return null;
        }

        try (var reader = new BufferedReader(new InputStreamReader(inputStream)))
        {
            var stringBuilder = new StringBuilder();

            String line;

            while (CommonUtil.isNonNull.test(line = reader.readLine()))
            {
                stringBuilder.append(line).append("\n");
            }

            JsonObject config = new JsonObject(stringBuilder.toString());

            promise.complete(config);
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());

            promise.fail(exception);
        }

        return promise.future();
    }
}
