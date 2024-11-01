package com.org.motadata.database;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/31/24 12:46 AM
 */

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.pgclient.PgConnectOptions;


public class DatabaseServiceProvider extends AbstractVerticle {

    private static final LoggerUtil LOGGER = new LoggerUtil(DatabaseServiceProvider.class);

    // Create PostgreSQL connection options
    private static final PgConnectOptions connectOptions = new PgConnectOptions()
            .setPort(5432)
            .setHost("localhost")
            .setDatabase("postgres")
            .setUser("harsh")
            .setPassword("Mind@123");

    // Pool options
    private static final PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5);

    // Create the pooled client
    private static final Pool pool = Pool.pool(Bootstrap.getVertx(), connectOptions, poolOptions);

    @Override
    public void start()
    {
        DatabaseService.create(pool, result ->
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

