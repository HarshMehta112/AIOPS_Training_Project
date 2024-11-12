package com.org.motadata;

import com.org.motadata.constant.Constants;
import com.org.motadata.database.DatabaseServiceProvider;
import com.org.motadata.engines.AvailabilityPollingEngine;
import com.org.motadata.engines.DatabaseEngine;
import com.org.motadata.engines.DiscoveryEngine;
import com.org.motadata.engines.MetricPollingEngine;
import com.org.motadata.service.PollingRouter;
import com.org.motadata.service.PollingTrigger;
import com.org.motadata.service.QueryBuilder;
import com.org.motadata.api.ApiServer;
import com.org.motadata.utils.ConfigLoaderUtil;
import com.org.motadata.utils.LoggerUtil;
import com.org.motadata.utils.VerticleDeployUtil;
import io.vertx.core.*;

import java.time.Duration;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 3:14 PM
 */
public class Bootstrap
{
    private static final LoggerUtil LOGGER = new LoggerUtil(Bootstrap.class);

    private static final int MAX_EXECUTION_TIME = 60;

    private static final int MAX_EVENT_LOOP_POOL_SIZE = 8;

    private static final int MAX_WORKER_POOL_SIZE = 8;

    private static final Vertx VERTX = Vertx.vertx(new VertxOptions()
            .setEventLoopPoolSize(MAX_EVENT_LOOP_POOL_SIZE)
            .setWorkerPoolSize(MAX_WORKER_POOL_SIZE)
            .setMaxWorkerExecuteTime(Duration.ofSeconds(MAX_EXECUTION_TIME).toNanos())
            .setWarningExceptionTime(Duration.ofSeconds(MAX_EXECUTION_TIME).toNanos()));

    public static Vertx getVertx()
    {
        return VERTX;
    }

    public static void main(String[] args)
    {
        try
        {
            ConfigLoaderUtil.init().onComplete(booleanAsyncResult ->
            {
                if(booleanAsyncResult.result())
                {
                    var workers = ConfigLoaderUtil.getConfigs().getJsonObject(Constants.WORKERS);

                    VERTX.deployVerticle(DatabaseServiceProvider.class.getName()).compose(handler->
                            VERTX.deployVerticle(DatabaseEngine.class.getName(),
                                    new DeploymentOptions().setWorkerPoolSize(workers.getInteger(Constants.DB_WORKER)))).compose(handler->
                            VERTX.deployVerticle(ApiServer.class.getName())).compose(handler->
                            VERTX.deployVerticle(QueryBuilder.class.getName(),
                                    new DeploymentOptions().setWorkerPoolSize(workers.getInteger(Constants.QUERY_BUILDER_WORKER)))).compose(handler->
                            VERTX.deployVerticle(DiscoveryEngine.class.getName(),
                                    new DeploymentOptions().setWorkerPoolSize(workers.getInteger(Constants.DISCOVERY_WORKER)))).compose(handler->
                            VERTX.deployVerticle(PollingTrigger.class.getName())).compose(handler->
                            VERTX.deployVerticle(PollingRouter.class.getName(),
                                    new DeploymentOptions().setWorkerPoolSize(workers.getInteger(Constants.POLLING_ROUTER_WORKER)))).compose(handler->
                            VERTX.deployVerticle(AvailabilityPollingEngine.class.getName(),
                                    new DeploymentOptions().setWorkerPoolSize(workers.getInteger(Constants.AVAILABILITY_POLLING_WORKER)))).compose(handler->
                            new VerticleDeployUtil(VERTX, Constants.METRIC_POLLING_REQUESTS, MetricPollingEngine.class.getName(),
                                    workers.getInteger(Constants.METRIC_POLLING_INSTANCES),
                                    workers.getInteger(Constants.METRIC_POLLING_WORKER)).deploy()).onComplete(asyncResult->
                            {
                                if (asyncResult.succeeded() && asyncResult.result())
                                {
                                    LOGGER.info("All verticles are deployed successfully...");
                                }
                                else
                                {
                                    LOGGER.error(asyncResult.cause().getMessage(),asyncResult.cause().getStackTrace());

                                    VERTX.close();
                                }
                            });
                }
                else
                {
                    LOGGER.error(booleanAsyncResult.cause().getMessage(),booleanAsyncResult.cause().getStackTrace());

                    VERTX.close();
                }
            });
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());

            VERTX.close();
        }
    }
}