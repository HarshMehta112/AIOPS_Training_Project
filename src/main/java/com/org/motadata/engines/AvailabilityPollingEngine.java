package com.org.motadata.engines;

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.CommonUtil;
import com.org.motadata.constant.Constants;
import com.org.motadata.utils.ConfigLoaderUtil;
import com.org.motadata.utils.LoggerUtil;
import com.org.motadata.utils.PluginExecutorUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


/**
 * This class have logic availability polling of devices.
 * I made logic that get the all devices from db (select * from tbl_monitor), and after dividing
 * into batches (size of batch is configurable through properties file) and spawning go process
 * I write a logic that getting result from go is one by one not in bulk,
 * adding in array and doing bulk insert operation.
 * */

public class AvailabilityPollingEngine extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(AvailabilityPollingEngine.class);

    @Override
    public void start(Promise<Void> startPromise)
    {
        Bootstrap.getVertx().eventBus().<JsonArray>localConsumer
                (Constants.AVAILABILITY_POLLING_REQUESTS, availabilityPollRequest ->
                        {
                            var availabilityPollContext = availabilityPollRequest.body();

                            if(CommonUtil.isNonNull.test(availabilityPollContext))
                            {
                                processRequests(availabilityPollContext);
                            }
                        }).exceptionHandler(exception->
                        {
                            LOGGER.error(exception.getMessage(),exception.getStackTrace());

                            startPromise.fail(exception);
                        });

        startPromise.complete();
    }

    private void processRequests(JsonArray pollRequestContext)
    {
        while (pollRequestContext.size() > 0)
        {
            var batch = CommonUtil.getBatchedData(pollRequestContext,
                    ConfigLoaderUtil.getConfigs().getJsonObject(Constants.POLLING_BATCH_CONFIG).getInteger(Constants.AVAILABILITY_POLLING_BATCH));

            Bootstrap.getVertx().executeBlocking(() ->
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
                }
                catch (Exception exception)
                {
                    LOGGER.error(exception.getMessage(),exception.getStackTrace());
                }

                return null;

            },false);
        }
    }
}
