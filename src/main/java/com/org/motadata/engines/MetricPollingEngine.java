package com.org.motadata.engines;

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.CommonUtil;
import com.org.motadata.constant.Constants;
import com.org.motadata.utils.ConfigLoaderUtil;
import com.org.motadata.utils.LoggerUtil;
import com.org.motadata.utils.PluginExecutorUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 1:53 PM
 */

/**
 * This class have a logic that when metric poll request received with monitorIds
 *
 * if monitor id present in credentialContext then no need to do the db
 * call for that particular device in db for credential profile.
 *
 * if not present then made a query and get data from db and put it into credentialContext
 *
 * and after dividing into batches (size of batch is configurable
 * through properties file) and spawning go process
 *
 * I write a logic that getting result from go is one by one not in bulk,
 * adding in array and doing bulk insert operation.
 * */

public class MetricPollingEngine extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(MetricPollingEngine.class);

    private static final HashMap<Integer,String> credentialContext = new HashMap<>();

    @Override
    public void start()
    {
        String consumerId = config().getString(Constants.ROUTING_KEY);

        vertx.eventBus().<ArrayList<Integer>>localConsumer(consumerId, metricPollRequest ->
        {
            var monitorIds = metricPollRequest.body();

            if(!monitorIds.isEmpty())
            {
                processRequests(monitorIds);
            }
        }).exceptionHandler(exception->LOGGER.error(exception.getMessage(),exception.getStackTrace()));
    }

    private Future<Boolean> updateCredentialContext(ArrayList<Integer> monitorIds)
    {
        Promise<Boolean> promise = Promise.promise();

        try
        {
            var condition = new StringBuilder();

            for(int monitorId : monitorIds)
            {
                if(!credentialContext.containsKey(monitorId))
                {
                    condition.append("d.id=").append(monitorId).append(" or ");
                }
            }

            if (condition.toString().endsWith(" or "))
            {
                condition.setLength(condition.length() - 4);
            }

            if(!condition.isEmpty())
            {
                var queryBuildContext = new JsonObject();

                queryBuildContext.put(Constants.DB_OPERATION_TYPE, Constants.SELECT_OPERATION)
                        .put(Constants.QUERY,Constants.METRIC_POLLING_DATA_QUERY.replace(Constants.HASH_SEPARATOR,condition));

                Bootstrap.getVertx().eventBus().<JsonArray>request(Constants.DB_REQUESTS, queryBuildContext, dbOperationReply ->
                {
                    if(dbOperationReply.succeeded())
                    {
                        var result = dbOperationReply.result().body();

                        CommonUtil.updateCredentialContext(result,credentialContext);

                        promise.complete(true);
                    }
                });
            }
            else
            {
                promise.complete(true);
            }
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());

            promise.fail(exception.getMessage());
        }

        return promise.future();
    }

    private void processRequests(ArrayList<Integer> monitorIds)
    {
        updateCredentialContext(monitorIds).onComplete(booleanAsyncResult ->
        {
            if(Boolean.TRUE.equals(booleanAsyncResult.result()))
            {
                var pollingContext = CommonUtil.getMetricPollingContext(credentialContext);

                while (pollingContext.size() > 0)
                {
                    var batch = CommonUtil.getBatchedData(pollingContext,
                            ConfigLoaderUtil.getMetricPollingBatchSize());

                    Bootstrap.getVertx().executeBlocking(promise ->
                    {
                        try
                        {
                            LOGGER.info("Remaining context size after batching... metric polling " + pollingContext.size());

                            if (!batch.isEmpty())
                            {
                                batch.set(0, batch.getJsonObject(0).put(Constants.DEVICE_TYPE, Constants.SSH)
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
        });
    }
}
