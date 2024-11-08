package com.org.motadata.api;

import com.org.motadata.Bootstrap;
import com.org.motadata.constant.Constants;
import com.org.motadata.impl.InitializeRouter;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 11/7/24 3:19 PM
 */
public class Monitor implements InitializeRouter
{
    private static final LoggerUtil LOGGER = new LoggerUtil(Monitor.class);

    @Override
    public void initRouter(Router router)
    {
        router.get("/" + "get" + "/:id").handler(this::getDeviceDetails);
    }

    public void getDeviceDetails(RoutingContext routingContext)
    {
        try
        {
            var monitorId = routingContext.request().getParam(Constants.ID);

            var dbOperationContext = new JsonObject();

            dbOperationContext.put(Constants.DB_OPERATION_TYPE,Constants.SELECT_OPERATION)
                    .put(Constants.DB_TABLE_NAME,Constants.METRIC_TABLE)
                    .put(Constants.QUERY,Constants.METRIC_GET_QUERY.replace(Constants.HASH_SEPARATOR,monitorId));

            Bootstrap.getVertx().eventBus().<JsonArray>request(Constants.DB_REQUESTS,dbOperationContext,
                    dbOperationReply ->
                    {
                        var responseMessage = dbOperationReply.result().body();

                        routingContext.response()
                                .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON)
                                .end(responseMessage.encodePrettily());
                    });
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }
    }

}
