package com.org.motadata.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;


/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/31/24 12:20 AM
 */


public class DatabaseServiceImpl implements DatabaseService
{

    private final Pool pool;

    public DatabaseServiceImpl(Pool pool, Handler<AsyncResult<DatabaseService>> handler) {
        this.pool = pool;
        handler.handle(Future.succeededFuture(this));
    }

    @Override
    public DatabaseService executeQuery(String query, JsonObject params, Handler<AsyncResult<Void>> resultHandler) {

        Tuple tuple = Tuple.tuple();

        if (params != null)
        {
            params.forEach(entry -> tuple.addValue(entry.getValue()));
        }

        pool.withConnection(connection -> connection
                .preparedQuery(query)
                .execute(tuple)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        resultHandler.handle(Future.succeededFuture());
                    } else {
                        resultHandler.handle(Future.failedFuture(res.cause()));
                    }
                }));
        return this;
    }

    @Override
    public DatabaseService executeSelect(String query, Handler<AsyncResult<JsonArray>> resultHandler) {
        pool.withConnection(connection -> connection
                .preparedQuery(query)
                .execute()
                .onComplete(res -> {
                    if (res.succeeded()) {
                        JsonArray results = new JsonArray();
                        res.result().forEach(row -> results.add(row.toJson()));
                        resultHandler.handle(Future.succeededFuture(results));
                    } else {
                        resultHandler.handle(Future.failedFuture(res.cause()));
                    }
                }));
        return this;
    }

    @Override
    public void close() {
        pool.close();
    }
}


