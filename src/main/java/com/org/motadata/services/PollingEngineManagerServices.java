package com.org.motadata.services;

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.CommonUtil;
import com.org.motadata.utils.Constants;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 11/4/24 10:21 AM
 */
public class PollingEngineManagerServices extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(CommonUtil.class);

    private static final Map<Integer,String> monitorIdToConsumer = new HashMap<>();

    @Override
    public void start()
    {
        //TODO harsh flow
        // get all devices -- if availibility polling send to AvailibilityPollingEngine
        // otherwise put the monitor id to verticle instance id

        Bootstrap.getVertx().eventBus().<String>localConsumer(Constants.POLLING_REQUESTS, pollRequest ->
        {
            var pollRequestType = pollRequest.body();

            Bootstrap.getVertx().executeBlocking(promise ->
            {
                switch (pollRequestType)
                {
                    case Constants.AVAILIBILITY_POLLING_TIME -> availibilityPollRequestHandler(promise);

                    case Constants.METRIC_POLLING_TIME -> metricPollRequestHandler(promise);

                    default -> throw new IllegalArgumentException("Wrong polling request type");
                }
            });

        });
    }


    private void availibilityPollRequestHandler(Promise<Object> promise)
    {
        try
        {
            var queryBuildContext = new JsonObject();

            queryBuildContext.put(Constants.DB_OPERATION_TYPE,Constants.SELECT_OPERATION);

            queryBuildContext.put(Constants.DB_TABLE_NAME,Constants.MONITOR_TABLE);

            CommonUtil.handleSelectRequest(queryBuildContext).onComplete(deviceContexts->
            {
                var result = deviceContexts.result();

                if(result == null)
                {
                    promise.fail("Get null context from database for availibility polling");
                }
                else
                {
                    Bootstrap.getVertx().eventBus().send(Constants.AVAILIBILITY_POLLING_REQUESTS,result);

                    promise.complete();
                }
            });


        }
        catch (Exception exception)
        {
            promise.fail(exception.getCause());

            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }
    }

    private void metricPollRequestHandler(Promise<Object> promise)
    {
        // getall devices, send to
    }
}
