package com.org.motadata.engines;

import com.org.motadata.Bootstrap;
import com.org.motadata.services.ConfigurationService;
import com.org.motadata.utils.Constants;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 1:55 PM
 */
public class DatabaseEngine extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(DatabaseEngine.class);

    @Override
    public void start()
    {
        Bootstrap.getVertx().eventBus().<JsonObject>localConsumer(Constants.DB_REQUESTS, handler ->

                Bootstrap.getVertx().executeBlocking(promise ->
                {
                    var queryBuildContext = handler.body();

                    LOGGER.info(queryBuildContext.encodePrettily());

                    if(queryBuildContext.getString(Constants.DB_OPERATION_TYPE).equals(Constants.SELECT_OPERATION))
                    {
                        ConfigurationService.getDatabaseServiceProxy()
                                .executeSelect(queryBuildContext.getString(Constants.QUERY),asyncResult ->
                        {
                            if(asyncResult.succeeded())
                            {
                                Bootstrap.getVertx().eventBus().send(handler.replyAddress(),asyncResult.result());
                            }
                            else
                            {
                                LOGGER.error("Some issue in building query .." + asyncResult.cause().getMessage()
                                        , asyncResult.cause().getStackTrace());
                            }
                        });
                    }
                    else
                    {
                        ConfigurationService.getDatabaseServiceProxy()
                                .executeQuery(queryBuildContext.getString(Constants.QUERY)
                                        ,queryBuildContext.getJsonObject(Constants.DB_VALUES),asyncResult ->
                                        {
                                            if(asyncResult.succeeded())
                                            {
                                                Bootstrap.getVertx().eventBus().send(handler.replyAddress(),true);
                                            }
                                            else
                                            {
                                                LOGGER.error("Some issue in building query .." + asyncResult.cause().getMessage()
                                                        , asyncResult.cause().getStackTrace());
                                            }
                                        });
                    }
                },false)).exceptionHandler(exceptionHandler->LOGGER.error(exceptionHandler.getMessage(),exceptionHandler.getStackTrace()));
    }
}