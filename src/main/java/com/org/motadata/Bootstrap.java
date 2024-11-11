package com.org.motadata;

import com.org.motadata.constant.Constants;
import com.org.motadata.database.DatabaseServiceProvider;
import com.org.motadata.engines.AvailibilityPollingEngine;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 3:14 PM
 */
public class Bootstrap
{
    private static final LoggerUtil LOGGER = new LoggerUtil(Bootstrap.class);

    private static final Vertx VERTX = Vertx.vertx(new VertxOptions()
            .setEventLoopPoolSize(8)
            .setWorkerPoolSize(16)
            .setMaxWorkerExecuteTime(Duration.ofSeconds(60).toNanos())
            .setWarningExceptionTime(Duration.ofSeconds(60).toNanos()));

    public static Vertx getVertx()
    {
        return VERTX;
    }

    private static final List<Future<String>> DEPLOYMENTS = new ArrayList<>();

    public static List<Future<String>> getDeployments()
    {
        return DEPLOYMENTS;
    }

    public static void main(String[] args)
    {
        ConfigLoaderUtil.init().onComplete(booleanAsyncResult ->
        {
            if(Boolean.TRUE.equals(booleanAsyncResult.result()))
            {
                var workers = ConfigLoaderUtil.getConfigs().getJsonObject(Constants.WORKERS);

                DEPLOYMENTS.add(VERTX.deployVerticle(DatabaseServiceProvider.class.getName()));

                DEPLOYMENTS.add(VERTX.deployVerticle(DatabaseEngine.class.getName(),
                        new DeploymentOptions().setWorkerPoolSize(workers.getInteger(Constants.DB_WORKER))));

                DEPLOYMENTS.add(VERTX.deployVerticle(ApiServer.class.getName()));

                DEPLOYMENTS.add(VERTX.deployVerticle(QueryBuilder.class.getName(),
                        new DeploymentOptions().setWorkerPoolSize(workers.getInteger(Constants.QUERY_BUILDER_WORKER))));

                DEPLOYMENTS.add(VERTX.deployVerticle(DiscoveryEngine.class.getName(),
                        new DeploymentOptions().setWorkerPoolSize(workers.getInteger(Constants.DISCOVERY_WORKER))));

                DEPLOYMENTS.add(VERTX.deployVerticle(PollingTrigger.class.getName()));

                DEPLOYMENTS.add(VERTX.deployVerticle(PollingRouter.class.getName(),
                        new DeploymentOptions().setWorkerPoolSize(workers.getInteger(Constants.POLLING_ROUTER_WORKER))));

                DEPLOYMENTS.add(VERTX.deployVerticle(AvailibilityPollingEngine.class.getName(),
                        new DeploymentOptions().setWorkerPoolSize(workers.getInteger(Constants.AVAILIBILITY_POLLING_WORKER))));

                new VerticleDeployUtil(VERTX,
                        Constants.METRIC_POLLING_REQUESTS, MetricPollingEngine.class.getName(),
                        workers.getInteger(Constants.METRIC_POLLING_INSTANCES),
                        workers.getInteger(Constants.METRIC_POLLING_WORKER)).deploy();

                CompositeFuture combinedFuture = Future.join(DEPLOYMENTS);

                combinedFuture.onComplete(asyncResult ->
                {
                    if (asyncResult.succeeded())
                    {
                        for (var index = 0; index < DEPLOYMENTS.size(); index++)
                        {
                            if (DEPLOYMENTS.get(index).succeeded())
                            {
                                LOGGER.info("Deployment " + DEPLOYMENTS.get(index) + " deployed successfully.. ");
                            }
                            else
                            {
                                LOGGER.error("Deployment " + index + " failed: " +
                                        DEPLOYMENTS.get(index).cause(),DEPLOYMENTS.get(index).cause().getStackTrace());

                                VERTX.close();
                            }
                        }
                    }
                    else
                    {
                        LOGGER.error("Some issue occurred in configurations loading.." + asyncResult.cause()
                                ,asyncResult.cause().getStackTrace());

                        VERTX.close();
                    }
                });
            }
            else
            {
                LOGGER.error("Some issue occurred in configurations loading.." + booleanAsyncResult.cause()
                        ,booleanAsyncResult.cause().getStackTrace());

                VERTX.close();
            }
        });
    }
}
