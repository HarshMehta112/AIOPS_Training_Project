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
            .setEventLoopPoolSize(ConfigLoaderUtil.getEventLoopWorker())
            .setWorkerPoolSize(ConfigLoaderUtil.getWorkerPoolHelper())
            .setMaxWorkerExecuteTime(Duration.ofSeconds(60).toNanos())
            .setWarningExceptionTime(Duration.ofSeconds(60).toNanos()));

    public static Vertx getVertx()
    {
        return VERTX;
    }

    private static final List<Future<String>> deployments = new ArrayList<>();

    public static List<Future<String>> getDeployments()
    {
        return deployments;
    }

    public static void main(String[] args)
    {
        ConfigLoaderUtil.init().onComplete(booleanAsyncResult ->
        {
            if(Boolean.TRUE.equals(booleanAsyncResult.result()))
            {
                deployments.add(VERTX.deployVerticle(DatabaseServiceProvider.class.getName()));

                deployments.add(VERTX.deployVerticle(DatabaseEngine.class.getName(),
                        new DeploymentOptions().setWorkerPoolSize(ConfigLoaderUtil.getDbWorker())));

                deployments.add(VERTX.deployVerticle(ApiServer.class.getName()));

                deployments.add(VERTX.deployVerticle(QueryBuilder.class.getName(),
                        new DeploymentOptions().setWorkerPoolSize(ConfigLoaderUtil.getQueryBuilderWorker())));

                deployments.add(VERTX.deployVerticle(DiscoveryEngine.class.getName(),
                        new DeploymentOptions().setWorkerPoolSize(ConfigLoaderUtil.getDiscoveryWorker())));

                deployments.add(VERTX.deployVerticle(PollingTrigger.class.getName()));

                deployments.add(VERTX.deployVerticle(PollingRouter.class.getName(),
                        new DeploymentOptions().setWorkerPoolSize(ConfigLoaderUtil.getPollingRouterWorker())));

                deployments.add(VERTX.deployVerticle(AvailibilityPollingEngine.class.getName(),
                        new DeploymentOptions().setWorkerPoolSize(ConfigLoaderUtil.getAvailibilityPollingWorker())));

                new VerticleDeployUtil(VERTX,
                        Constants.METRIC_POLLING_REQUESTS, MetricPollingEngine.class.getName(),ConfigLoaderUtil.getMetricPollingInstances()
                        ,ConfigLoaderUtil.getMetricPollingWorker()).deploy();

                CompositeFuture combinedFuture = Future.join(deployments);

                combinedFuture.onComplete(asyncResult ->
                {
                    if (asyncResult.succeeded())
                    {
                        System.out.println("All deployments completed successfully.");

                        for (int index = 0; index < deployments.size(); index++)
                        {
                            if (deployments.get(index).succeeded())
                            {
                                LOGGER.info("Deployment " + deployments.get(index) + " deployed successfully.. ");
                            }
                            else
                            {
                                LOGGER.error("Deployment " + index + " failed: " +
                                        deployments.get(index).cause(),deployments.get(index).cause().getStackTrace());

                                VERTX.close();
                            }
                        }
                    }
                    else
                    {
                        LOGGER.error("Some issue occurred in configurations loading.." + asyncResult.cause()
                                ,asyncResult.cause().getStackTrace());
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
