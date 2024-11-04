package com.org.motadata.engines;

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.CommonUtil;
import com.org.motadata.utils.Constants;
import com.org.motadata.utils.LoggerUtil;
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

                            if(CommonUtil.isValidResult.test(availibilityPollContext))
                            {
                                processRequests(availibilityPollRequest.body());
                            }
                        }).exceptionHandler(exception->LOGGER.error(exception.getMessage(),exception.getStackTrace()));
    }

    private void processRequests(JsonArray pollRequestContext)
    {
        if (pollRequestContext.size() > 0)
        {
            Bootstrap.getVertx().executeBlocking(promise ->
            {
                try
                {
                    var batch = getBatchedData(pollRequestContext);

                    LOGGER.info("Remaining context size after batching... " + pollRequestContext.size());

                    if (!batch.isEmpty())
                    {
                        batch.set(0, batch.getJsonObject(0).put(Constants.DEVICE_TYPE, Constants.PING)
                                .put(Constants.PLUGIN_CALL_CATEGORY,Constants.POLLING));

                        var result = CommonUtil.executePlugin(batch);

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

    private JsonArray getBatchedData(JsonArray pollingContext)
    {
        var batch = new JsonArray();

        int maxBatchSize = Math.min(2, pollingContext.size());

        for (int index = 0; index < maxBatchSize; index++)
        {
            batch.add(pollingContext.getJsonObject(0));

            pollingContext.remove(0);
        }

        return batch;
    }
}
