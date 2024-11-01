package com.org.motadata.impl;

import io.vertx.ext.web.RoutingContext;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/31/24 10:30 AM
 */

public interface CrudOperations
{
    void create(RoutingContext routingContext);
    void getAll(RoutingContext routingContext);
    void update(RoutingContext routingContext);
    void delete(RoutingContext routingContext);
}
