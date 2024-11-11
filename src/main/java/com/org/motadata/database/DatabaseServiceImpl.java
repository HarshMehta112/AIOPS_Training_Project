package com.org.motadata.database;

import com.org.motadata.constant.Constants;
import com.org.motadata.utils.CommonUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;


/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/31/24 12:20 AM
 */


public class DatabaseServiceImpl implements DatabaseService
{
    private final Pool pool;

    public DatabaseServiceImpl(Pool pool, Handler<AsyncResult<DatabaseService>> handler)
    {
        this.pool = pool;

        handler.handle(Future.succeededFuture(this));
    }

    @Override
    public DatabaseService executeQuery(String query, JsonObject params, Handler<AsyncResult<Void>> resultHandler) {

        var tuple = Tuple.tuple();

        if (CommonUtil.isNonNull.test(params))
        {
            params.forEach(entry -> tuple.addValue(entry.getValue()));
        }

        pool.withConnection(connection -> connection
                .preparedQuery(query)
                .execute(tuple)
                .onComplete(result ->
                {
                    if (result.succeeded())
                    {
                        resultHandler.handle(Future.succeededFuture());
                    }
                    else
                    {
                        resultHandler.handle(Future.failedFuture(result.cause()));
                    }
                }));

        return this;
    }

    @Override
    public DatabaseService executeSelect(String query, Handler<AsyncResult<JsonArray>> resultHandler)
    {
        pool.withConnection(connection -> connection
                .preparedQuery(query)
                .execute()
                .onComplete(result ->
                {
                    if (result.succeeded())
                    {
                        var results = new JsonArray();

                        result.result().forEach(row -> results.add(row.toJson()));

                        resultHandler.handle(Future.succeededFuture(results));
                    }
                    else
                    {
                        resultHandler.handle(Future.failedFuture(result.cause()));
                    }
                }));
        return this;
    }


    @Override
    public DatabaseService batchInsertMetrics(JsonArray batchContext, Handler<AsyncResult<Void>> resultHandler)
    {
        if(CommonUtil.isNonNull.test(batchContext))
        {
            // Prepare a list of tuples for batch execution
            var batch = new ArrayList<Tuple>();

            for (var index=0; index<batchContext.size(); index++)
            {
                var context = batchContext.getJsonObject(index);

                if(CommonUtil.isNonNull.test(context) && !context.isEmpty())
                {
                    var monitorId = context.getInteger(Constants.MONITOR_ID);

                    context.remove(Constants.MONITOR_ID);

                    for (var entry : context)
                    {
                        var metricName = entry.getKey();

                        var metricValue = entry.getValue();

                        // Add to the batch
                        batch.add(Tuple.of(monitorId, metricName, metricValue.toString())); // Convert value to String
                    }
                }
            }

            if (!batch.isEmpty())
            {
                // Execute the batch insert
                pool.withConnection(connection -> connection
                        .preparedQuery(Constants.METRIC_INSERT_QUERY)
                        .executeBatch(batch)
                        .onComplete(res ->
                        {
                            if (res.succeeded())
                            {
                                resultHandler.handle(Future.succeededFuture());
                            }
                            else
                            {
                                resultHandler.handle(Future.failedFuture(res.cause()));
                            }
                        }));
            }
            else
            {
                resultHandler.handle(Future.succeededFuture());
            }
        }

        return this;
    }

    @Override
    public void close()
    {
        pool.close();
    }
}


