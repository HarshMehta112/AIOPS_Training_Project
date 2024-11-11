package com.org.motadata.utils;

import com.org.motadata.Bootstrap;
import com.org.motadata.constant.Constants;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 11/6/24 4:27 PM
 */
public class HandleRequestUtil
{
    public static void handleModificationRequest(JsonObject queryBuildContext, RoutingContext routingContext,
                                                 String successMessage, String failureMessage)
    {
        Bootstrap.getVertx().eventBus().<String>request(Constants.QUERY_BUILD_REQUEST, queryBuildContext, queryBuilderReply ->
        {
            if (queryBuilderReply.succeeded())
            {
                queryBuildContext.put(Constants.QUERY, queryBuilderReply.result().body());

                Bootstrap.getVertx().eventBus().<Boolean>request(Constants.DB_REQUESTS, queryBuildContext, dbOperationReply ->
                {
                    var responseMessage = dbOperationReply.result().body()
                            ? successMessage : failureMessage;

                    routingContext.response()
                            .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN)
                            .end(responseMessage);
                });
            }
        });
    }

    public static void handleSelectRequest(JsonObject queryBuildContext, RoutingContext routingContext)
    {
        Bootstrap.getVertx().eventBus().<String>request(Constants.QUERY_BUILD_REQUEST,queryBuildContext,
                queryBuilderReply->
                {
                    if(queryBuilderReply.succeeded())
                    {
                        queryBuildContext.put(Constants.QUERY,queryBuilderReply.result().body());

                        Bootstrap.getVertx().eventBus().<JsonArray>request(Constants.DB_REQUESTS,queryBuildContext,
                                dbOperationReply ->
                                {
                                    if(dbOperationReply.succeeded())
                                    {
                                        routingContext.response()
                                                .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON)
                                                .end(dbOperationReply.result().body().encodePrettily());
                                    }
                                });
                    }
                });
    }

    public static Future<JsonArray> handleSelectRequest(JsonObject queryBuildContext)
    {
        var promise = Promise.<JsonArray>promise();

        Bootstrap.getVertx().eventBus().<String>request(Constants.QUERY_BUILD_REQUEST, queryBuildContext, queryBuilderReply -> {
            if (queryBuilderReply.succeeded())
            {
                queryBuildContext.put(Constants.QUERY, queryBuilderReply.result().body());

                Bootstrap.getVertx().eventBus().<JsonArray>request(Constants.DB_REQUESTS, queryBuildContext, dbOperationReply -> {
                    if (dbOperationReply.succeeded())
                    {
                        promise.complete(dbOperationReply.result().body());
                    }
                    else
                    {
                        promise.fail(dbOperationReply.cause());
                    }
                });
            }
            else
            {
                promise.fail(queryBuilderReply.cause());
            }
        });

        return promise.future();
    }

}
