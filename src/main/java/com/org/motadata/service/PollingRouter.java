package com.org.motadata.service;

import com.org.motadata.Bootstrap;
import com.org.motadata.constant.Constants;
import com.org.motadata.utils.ConfigLoaderUtil;
import com.org.motadata.utils.HandleRequestUtil;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * For metric polling I implemented config logic and storing into one hashmap of monitorId=consumerName
 * to do that after processing db data (select * from tbl_monitor) it will create consumerName = JsonArray of monitor Ids
 * and send to particular consumer.
 * */

public class PollingRouter extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(PollingRouter.class);

    private static final Map<Integer,String> monitorIdToConsumer = new HashMap<>();

    private static final Map<String,ArrayList<Integer>> consumerAddressToDevices = new HashMap<>();

    @Override
    public void start(Promise<Void> startPromise)
    {
        Bootstrap.getVertx().eventBus().<String>localConsumer(Constants.POLLING_REQUESTS, pollRequest ->
        {
            var pollRequestType = pollRequest.body();

            Bootstrap.getVertx().executeBlocking(() ->
            {
                try
                {
                    switch (pollRequestType)
                    {
                        case Constants.AVAILABILITY_POLLING_TIME -> getDeviceData().onComplete(handler->
                        {
                            try
                            {
                                if(handler.succeeded())
                                {
                                    Bootstrap.getVertx().eventBus().send(Constants.AVAILABILITY_POLLING_REQUESTS,handler.result());
                                }
                                else
                                {
                                    LOGGER.error(handler.cause().getMessage(),handler.cause().getStackTrace());
                                }
                            }
                            catch (Exception exception)
                            {
                                LOGGER.error(exception.getMessage(),exception.getStackTrace());
                            }
                        });

                        case Constants.METRIC_POLLING_TIME -> metricPollRequestHandler();

                        default -> throw new IllegalArgumentException("Wrong polling request type");
                    }
                }
                catch (Exception exception)
                {
                    LOGGER.error(exception.getMessage(),exception.getStackTrace());

                    startPromise.fail(exception);
                }

                return null;
            });

        }).exceptionHandler(exception->
        {
            LOGGER.error(exception.getMessage(), exception.getStackTrace());

            startPromise.fail(exception);
        });

        startPromise.complete();
    }


    private Future<JsonArray> getDeviceData()
    {
        var promise = Promise.<JsonArray>promise();

        try
        {
            var queryBuildContext = new JsonObject();

            queryBuildContext.put(Constants.DB_OPERATION_TYPE, Constants.SELECT_OPERATION)
                    .put(Constants.DB_TABLE_NAME, Constants.MONITOR_TABLE);

            HandleRequestUtil.handleSelectRequest(queryBuildContext).onComplete(deviceContexts ->
            {
                var result = deviceContexts.result();

                if (result == null)
                {
                    promise.fail("Get null context from database for polling....");
                }
                else
                {
                    promise.complete(result);
                }
            });
        }
        catch (Exception exception)
        {
            promise.fail(exception.getCause());

            LOGGER.error(exception.getMessage(), exception.getStackTrace());
        }

        return promise.future();
    }


    private void metricPollRequestHandler()
    {
        try
        {
            getDeviceData().onComplete(dbOperationReply->
            {
                if(dbOperationReply.succeeded())
                {
                    var devicesContext = dbOperationReply.result();

                    String consumerAddress;

                    for(var index=0; index<devicesContext.size();index++)
                    {
                        var monitorId = devicesContext.getJsonObject(index).getInteger(Constants.ID);

                        // If ID is already mapped to a consumer, route to the same consumer
                        consumerAddress = monitorIdToConsumer.computeIfAbsent(monitorId,
                                logic -> assignConsumerForMonitorId());

                        var deviceArray = consumerAddressToDevices.
                                computeIfAbsent(consumerAddress, value -> new ArrayList<>());

                        deviceArray.add(monitorId);
                    }

                    for (var entry : consumerAddressToDevices.entrySet())
                    {
                        Bootstrap.getVertx().eventBus().send(entry.getKey(), entry.getValue());
                    }

                    LOGGER.info(consumerAddressToDevices.toString());

                    LOGGER.info(monitorIdToConsumer.toString());

                    //clear the map to reduce memory
                    consumerAddressToDevices.clear();
                }
                else
                {
                    LOGGER.error("Get null context from database for polling...."+dbOperationReply.cause().getMessage(),dbOperationReply.cause().getStackTrace());
                }

            });
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(), exception.getStackTrace());
        }
    }

    private String assignConsumerForMonitorId()
    {
        // we can our own load balancing logic if needed
        var consumerIndex = new Random().nextInt(ConfigLoaderUtil.getConfigs().getJsonObject(Constants.WORKERS)
                .getInteger(Constants.METRIC_POLLING_INSTANCES)) + 1;

        return Constants.METRIC_POLLING_REQUESTS + consumerIndex;
    }
}
