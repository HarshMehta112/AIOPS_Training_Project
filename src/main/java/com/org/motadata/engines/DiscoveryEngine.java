package com.org.motadata.engines;

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.CommonUtil;
import com.org.motadata.constant.Constants;
import com.org.motadata.utils.LoggerUtil;
import com.org.motadata.utils.PluginExecutorUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 11/1/24 11:20 PM
 */
public class DiscoveryEngine extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(DatabaseEngine.class);

    @Override
    public void start()
    {

        Bootstrap.getVertx().eventBus().<JsonArray>localConsumer(Constants.DISCOVERY_RUN_REQUEST,jsonArrayMessage ->

                Bootstrap.getVertx().executeBlocking(() ->
                {
                    try
                    {
                        var discoveryResultContext = PluginExecutorUtil.executePlugin(jsonArrayMessage.body()).getJsonObject(0);

                        var discoveryId = discoveryResultContext.getString(Constants.DISCOVERY_ID);

                        var discoveryResult = discoveryResultContext.getString(Constants.STATUS);

                        if(discoveryResult != null)
                        {
                            var queryBuildContext = new JsonObject();

                            queryBuildContext.put(Constants.DB_OPERATION_TYPE, Constants.UPDATE_OPERATION)
                                    .put(Constants.DB_TABLE_NAME, Constants.DISCOVERY_PROFILE_TABLE)
                                    .put(Constants.DB_CONDITIONS, CommonUtil
                                            .buildString(Constants.ID, " = ", discoveryId))
                                    .put(Constants.DB_VALUES, new JsonObject()
                                    .put(Constants.DISCOVERED_FLAG, discoveryResult.equals("success")));

                            Bootstrap.getVertx().eventBus().<String>request(Constants.QUERY_BUILD_REQUEST,
                                    queryBuildContext, queryBuilderReply ->
                            {
                                if (queryBuilderReply.succeeded())
                                {
                                    queryBuildContext.put(Constants.QUERY, queryBuilderReply.result().body());

                                    Bootstrap.getVertx().eventBus().<Boolean>request(Constants.DB_REQUESTS,
                                            queryBuildContext, dbOperationReply ->
                                    {
                                        if(dbOperationReply.result().body())
                                        {
                                            Bootstrap.getVertx().eventBus().send(jsonArrayMessage.replyAddress(),discoveryResult);
                                        }
                                        else
                                        {
                                            Bootstrap.getVertx().eventBus().send(jsonArrayMessage.replyAddress(),discoveryResult);
                                        }
                                    });
                                }
                            });
                        }
                    }
                    catch (Exception exception)
                    {
                        LOGGER.error(exception.getMessage(),exception.getStackTrace());
                    }
                    return null;
                }));
    }
}
