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
 * Date: 10/29/24 1:53 PM
 */
public class AvailibilityPollingEngine extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(AvailibilityPollingEngine.class);

    @Override
    public void start()
    {
        Bootstrap.getVertx().eventBus().<JsonArray>localConsumer
                (Constants.AVAILIBILITY_POLLING_REQUESTS, availibilityPollRequest ->
                        {
                            var availibilityPollContext = availibilityPollRequest.body();

                            if(CommonUtil.isNonNull.test(availibilityPollContext))
                            {
                                processRequests(availibilityPollContext);
                            }
                        }).exceptionHandler(exception->LOGGER.error(exception.getMessage(),exception.getStackTrace()));
    }

    private void processRequests(JsonArray pollRequestContext)
    {
        while (pollRequestContext.size() > 0)
        {
            var batch = CommonUtil.getBatchedData(pollRequestContext);

            Bootstrap.getVertx().executeBlocking(promise ->
            {
                try
                {
                    LOGGER.info("Remaining context size after batching... " + pollRequestContext.size());

                    if (!batch.isEmpty())
                    {
                        batch.set(0, batch.getJsonObject(0).put(Constants.DEVICE_TYPE, Constants.PING)
                                .put(Constants.PLUGIN_CALL_CATEGORY,Constants.POLLING));

                        var result = PluginExecutorUtil.executePlugin(batch);

                        var dbOperationContext = new JsonObject()
                                .put(Constants.DB_OPERATION_TYPE,Constants.BATCH_INSERT_OPERATION)
                                .put(Constants.DB_VALUES,result);

                        Bootstrap.getVertx().eventBus().send(Constants.DB_REQUESTS,dbOperationContext);
                    }

                    promise.complete();
                }
                catch (Exception exception)
                {
                    promise.fail(exception.getCause());

                    LOGGER.error(exception.getMessage(),exception.getStackTrace());
                }

            },false);
        }
    }
}
