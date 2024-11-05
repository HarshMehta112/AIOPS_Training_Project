package com.org.motadata;

import com.org.motadata.database.DatabaseServiceProvider;
import com.org.motadata.engines.AvailibilityPollingEngine;
import com.org.motadata.engines.DatabaseEngine;
import com.org.motadata.engines.DiscoveryEngine;
import com.org.motadata.engines.MetricPollingEngine;
import com.org.motadata.services.PollingEngineManagerServices;
import com.org.motadata.services.PollingTriggerServices;
import com.org.motadata.services.QueryBuilderServices;
import com.org.motadata.services.RoutingServices;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 3:14 PM
 */
public class Bootstrap
{
    private static final Vertx VERTX = Vertx.vertx();

    public static Vertx getVertx()
    {
        return VERTX;
    }

    public static void main(String[] args)
    {
        VERTX.deployVerticle(DatabaseServiceProvider.class.getName());

        //TODO HARSH READ FROM CONFIG WORKER POOL SIZE

        VERTX.deployVerticle(DatabaseEngine.class.getName(),
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER).setWorkerPoolSize(5));

        VERTX.deployVerticle(RoutingServices.class.getName());

        VERTX.deployVerticle(QueryBuilderServices.class.getName(),
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER).setWorkerPoolSize(2));

        VERTX.deployVerticle(DiscoveryEngine.class.getName(),
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER).setWorkerPoolSize(2));

        VERTX.deployVerticle(PollingTriggerServices.class.getName());

        VERTX.deployVerticle(PollingEngineManagerServices.class.getName(), new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER).setWorkerPoolSize(20));
        VERTX.deployVerticle(AvailibilityPollingEngine.class.getName(), new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER).setWorkerPoolSize(20));


        for (int index = 1; index <= 2; index++)
        {
            JsonObject config = new JsonObject()
                    .put("RoutingKey", "consumer-" + index);  // Unique consumer ID

            DeploymentOptions options = new DeploymentOptions().setConfig(config);

            VERTX.deployVerticle(MetricPollingEngine.class.getName(), options);
        }

    }
}
