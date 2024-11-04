package com.org.motadata.database;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 1:52 PM
 */


@ProxyGen
public interface DatabaseService {

    @GenIgnore
    static void create(Pool pool, Handler<AsyncResult<DatabaseService>> handler) {
        new DatabaseServiceImpl(pool, handler);
    }

    @GenIgnore
    static DatabaseService createProxy(Vertx vertx, String address) {
        return new DatabaseServiceVertxEBProxy(vertx, address);
    }
    @Fluent
    DatabaseService executeQuery(String query, JsonObject params, Handler<AsyncResult<Void>> resultHandler);

    @Fluent
    DatabaseService executeSelect(String query, Handler<AsyncResult<JsonArray>> resultHandler);

    @Fluent
    DatabaseService batchInsertMetrics(JsonArray metricsArray,Handler<AsyncResult<Void>> resultHandler);
    void close();
}

