package com.org.motadata.database;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/31/24 12:46 AM
 */

import com.org.motadata.Bootstrap;
import com.org.motadata.constant.Constants;
import com.org.motadata.utils.ConfigLoaderUtil;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.pgclient.PgConnectOptions;


public class DatabaseServiceProvider extends AbstractVerticle {

    private static final LoggerUtil LOGGER = new LoggerUtil(DatabaseServiceProvider.class);

    // Create PostgreSQL connection options
    private static final PgConnectOptions CONNECT_OPTIONS = new PgConnectOptions()
            .setPort(ConfigLoaderUtil.getDbPort())
            .setHost(ConfigLoaderUtil.getDbHost())
            .setDatabase(ConfigLoaderUtil.getDbName())
            .setUser(ConfigLoaderUtil.getDbUsername())
            .setPassword(ConfigLoaderUtil.getDbPassword())
            .setReconnectAttempts(3).setReconnectInterval(2000L)
            .setSsl(true).setSslMode(SslMode.REQUIRE)
            .setPemTrustOptions(new PemTrustOptions()
                    .addCertPath(Constants.DB_SSL_CERT_PATH));

    // Pool options
    private static final PoolOptions POOL_OPTIONS = new PoolOptions()
            .setMaxSize(ConfigLoaderUtil.getDbMaxConnections());

    // Create the pooled client
    private static final Pool POOL = Pool.pool(Bootstrap.getVertx(), CONNECT_OPTIONS, POOL_OPTIONS);

    @Override
    public void start()
    {
        DatabaseService.create(POOL, result ->
        {
            try
            {
                if (result.succeeded())
                {
                    new ServiceBinder(vertx)
                            .setAddress("database.service.address")
                            .registerLocal(DatabaseService.class, result.result());
                }
                else
                {
                    LOGGER.error("Failed to start database service: "+ result.cause().getMessage(), result.cause().getStackTrace());
                }
            }
            catch (Exception exception)
            {
                LOGGER.error(exception.getMessage(),exception.getStackTrace());
            }
        });
    }
}

