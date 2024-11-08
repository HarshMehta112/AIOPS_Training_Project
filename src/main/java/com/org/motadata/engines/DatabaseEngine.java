package com.org.motadata.engines;

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.ConfigLoaderUtil;
import com.org.motadata.constant.Constants;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 1:55 PM
 */


/**
 * This class have logic of executing the db queries and return the result in async way.
 * this class only one that have execute the query.
 * */

public class DatabaseEngine extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(DatabaseEngine.class);

    @Override
    public void start()
    {
        Bootstrap.getVertx().eventBus().<JsonObject>localConsumer(Constants.DB_REQUESTS, handler ->

                Bootstrap.getVertx().executeBlocking(() ->
                {
                    try
                    {
                        var queryBuildContext = handler.body();

                        LOGGER.info(queryBuildContext.encodePrettily());

                        if(queryBuildContext.getString(Constants.DB_OPERATION_TYPE).equals(Constants.SELECT_OPERATION))
                        {
                            ConfigLoaderUtil.getDatabaseServiceProxy()
                                    .executeSelect(queryBuildContext.getString(Constants.QUERY),asyncResult ->
                                    {
                                        if(asyncResult.succeeded())
                                        {
                                            Bootstrap.getVertx().eventBus().send(handler.replyAddress(),asyncResult.result());
                                        }
                                        else
                                        {
                                            LOGGER.error("Some issue in executing query .." + asyncResult.cause().getMessage()
                                                    , asyncResult.cause().getStackTrace());
                                        }
                                    });
                        }
                        else if(queryBuildContext.getString(Constants.DB_OPERATION_TYPE).equals(Constants.BATCH_INSERT_OPERATION))
                        {
                            ConfigLoaderUtil.getDatabaseServiceProxy()
                                    .batchInsertMetrics(queryBuildContext.getJsonArray(Constants.DB_VALUES)
                                            ,asyncResult ->
                                            {
                                                if(asyncResult.failed())
                                                {
                                                    LOGGER.error("Some issue in executing batch insertion query .." + asyncResult.cause().getMessage()
                                                            , asyncResult.cause().getStackTrace());
                                                }
                                            });
                        }
                        else
                        {
                            ConfigLoaderUtil.getDatabaseServiceProxy()
                                    .executeQuery(queryBuildContext.getString(Constants.QUERY)
                                            ,queryBuildContext.getJsonObject(Constants.DB_VALUES),asyncResult ->
                                            {
                                                if(asyncResult.succeeded())
                                                {
                                                    Bootstrap.getVertx().eventBus().send(handler.replyAddress(),true);
                                                }
                                                else
                                                {
                                                    Bootstrap.getVertx().eventBus().send(handler.replyAddress(),false);

                                                    LOGGER.error("Some issue in executing query .." + asyncResult.cause().getMessage()
                                                            , asyncResult.cause().getStackTrace());

                                                }
                                            });
                        }
                    }
                    catch (Exception exception)
                    {
                        LOGGER.error(exception.getMessage(),exception.getStackTrace());
                    }

                    return null;

                },false));
    }
}