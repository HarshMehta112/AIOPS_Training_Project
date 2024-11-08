package com.org.motadata.utils;

import com.org.motadata.Bootstrap;
import com.org.motadata.constant.Constants;
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
    private final String className;

    private final int noOfInstances;

    private final Vertx vertx;

    private final String routingKey;

    private final int noOfWorkers;

    public VerticleDeployUtil(Vertx vertx,String routingKey, String className, int noOfInstances, int noOfWorkers)
    {
        this.vertx = vertx;

        this.routingKey = routingKey;

        this.className = className;

        this.noOfInstances = noOfInstances;

        this.noOfWorkers = noOfWorkers;
    }

    public void deploy()
    {
        for (int index = 1; index <= noOfInstances; index++)
        {
            // Create a configuration for each instance of the verticle
            JsonObject config = new JsonObject()
                    .put(Constants.ROUTING_KEY, routingKey + index);  // Unique consumer ID

            DeploymentOptions options = new DeploymentOptions().setConfig(config).setWorkerPoolSize(noOfWorkers);

            Bootstrap.getDeployments().add(vertx.deployVerticle(className, options));
        }
    }
}
