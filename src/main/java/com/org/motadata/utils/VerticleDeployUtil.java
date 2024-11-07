package com.org.motadata.utils;

import com.org.motadata.Bootstrap;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 11/7/24 10:52 AM
 */
public class VerticleDeployUtil
{
    private String className;

    private int noOfInstances;

    private Vertx vertx;

    private String routingKey;

    private int noOfWorkers;

    public VerticleDeployUtil(Vertx vertx,String routingKey, String className, int noOfInstances, int noOfWorkers)
    {
        this.vertx = vertx;

        this.routingKey = routingKey;

        this.className = className;

        this.noOfInstances = noOfInstances;

        this.noOfWorkers = noOfWorkers;
    }

    public Future<CompositeFuture> deploy()
    {
        List<Future> futures = new ArrayList<>();

        for (int index = 1; index <= noOfInstances; index++)
        {
            // Create a configuration for each instance of the verticle
            JsonObject config = new JsonObject()
                    .put("RoutingKey", routingKey + index);  // Unique consumer ID

            DeploymentOptions options = new DeploymentOptions().setConfig(config).setWorkerPoolSize(noOfWorkers);

            futures.add(vertx.deployVerticle(className, options));
        }

        return CompositeFuture.all(futures);
    }
}
