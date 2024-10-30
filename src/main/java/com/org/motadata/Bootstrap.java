package com.org.motadata;

import com.org.motadata.services.RoutingServices;
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
        VERTX.deployVerticle(RoutingServices.class.getName());
    }
}
