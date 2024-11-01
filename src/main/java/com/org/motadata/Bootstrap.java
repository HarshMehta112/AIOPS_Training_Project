package com.org.motadata;

import com.org.motadata.database.DatabaseServiceProvider;
import com.org.motadata.engines.DatabaseEngine;
import com.org.motadata.services.QueryBuilderServices;
import com.org.motadata.services.RoutingServices;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;

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

        VERTX.deployVerticle(DatabaseEngine.class.getName(),
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER).setWorkerPoolSize(5));

        VERTX.deployVerticle(RoutingServices.class.getName());

        VERTX.deployVerticle(QueryBuilderServices.class.getName(),
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER).setWorkerPoolSize(2));

    }
}
