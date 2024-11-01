package com.org.motadata.services;

import com.org.motadata.Bootstrap;
import com.org.motadata.database.DatabaseService;
import com.org.motadata.utils.CommonUtil;
import com.org.motadata.utils.Constants;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/30/24 1:08 PM
 */
public class ConfigurationService
{
    private static final LoggerUtil LOGGER = new LoggerUtil(ConfigurationService.class);

    private static JWTAuth jwtAuth;
    private static String loginUsername;

    private static String loginPassword;

    private static DatabaseService databaseServiceProxy;

    public static DatabaseService getDatabaseServiceProxy() {
        return databaseServiceProxy;
    }

    public static void setDatabaseServiceProxy(DatabaseService databaseServiceProxy) {
        ConfigurationService.databaseServiceProxy = databaseServiceProxy;
    }

    public static String getSslKeystorePath() {
        return sslKeystorePath;
    }

    public static void setSslKeystorePath(String sslKeystorePath) {
        ConfigurationService.sslKeystorePath = sslKeystorePath;
    }

    public static String getSslKeystorePassword() {
        return sslKeystorePassword;
    }

    public static void setSslKeystorePassword(String sslKeystorePassword) {
        ConfigurationService.sslKeystorePassword = sslKeystorePassword;
    }

    private static String sslKeystorePath;

    private static String sslKeystorePassword;

    private ConfigurationService() {}

    public static String getLoginUsername() {
        return loginUsername;
    }

    public static void setLoginUsername(String loginUsername) {
        ConfigurationService.loginUsername = loginUsername;
    }

    public static String getLoginPassword() {
        return loginPassword;
    }

    public static void setLoginPassword(String loginPassword) {
        ConfigurationService.loginPassword = loginPassword;
    }

    public static JWTAuth getJwtAuth() {
        return jwtAuth;
    }

    public static void setJwtAuth(JWTAuth jwtAuth) {
        ConfigurationService.jwtAuth = jwtAuth;
    }

    static
    {
        init();
    }

    private static void init()
    {
        // Load properties file
        var properties = new Properties();

        try (FileInputStream inputStream = new FileInputStream(CommonUtil.buildString
                (Constants.CURRENT_DIR,Constants.PATH_SEPARATOR,Constants.PROPERTIES_FILE)))
        {

            properties.load(inputStream);

            setLoginUsername(properties.getProperty(Constants.USER_NAME));

            setLoginPassword(properties.getProperty(Constants.PASSWORD));

            LoggerUtil.setLogLevel(Boolean.parseBoolean(properties.getProperty
                    (Constants.DEBUG_LOG_FLAG))?Level.FINE:Level.INFO);

            LoggerUtil.setDebugEnabled(Boolean.parseBoolean(properties.getProperty
                            (Constants.DEBUG_LOG_FLAG)));

            setSslKeystorePath(properties.getProperty(Constants.SSL_KEYSTORE_PATH));

            setSslKeystorePassword(properties.getProperty(Constants.SSL_KEYSTORE_PASSWORD));

            // Get paths for public and private keys
            var publicKeyPath = properties.getProperty("publicKeyPath");

            var privateKeyPath = properties.getProperty("privateKeyPath");

            if (publicKeyPath == null || privateKeyPath == null)
            {
                LOGGER.warn("Key paths are not set in config.properties");

                return;
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

            DatabaseService databaseServiceProxy = DatabaseService.createProxy(Bootstrap.getVertx(), "database.service.address");

            setDatabaseServiceProxy(databaseServiceProxy);

            LOGGER.info("configurations loaded and setting up of configurations completed..");

        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());

            LOGGER.warn("Failed to load config.properties");
        }
    }


}
